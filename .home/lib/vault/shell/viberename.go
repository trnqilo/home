// go build -O2 -trimpath -ldflags="-s -w" -o viberename viberename.go
// ./viberename -root /path/to/dir -from "OldCo" -to "NewCo" -workers 16 # -n == preview
// ./viberename -root . -from foo -to bar -rename=false -backup
// ./viberename -root . -from v1 -to v2 -textonly -hidden=false -maxreadmb 256

package main

import (
	"bytes"
	"flag"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"runtime"
	"sort"
	"strings"
	"sync"
	"time"
)

type Job struct {
	path string
}

func main() {
	root := flag.String("root", ".", "Root folder to process")
	from := flag.String("from", "", "String to replace (required)")
	to := flag.String("to", "", "Replacement string (required)")
	workers := flag.Int("workers", max(2, runtime.NumCPU()), "Number of concurrent workers for content editing")
	dryRun := flag.Bool("n", false, "Dry-run (preview actions only)")
	rename := flag.Bool("rename", true, "Rename file and folder names")
	edit := flag.Bool("edit", true, "Edit file contents")
	backups := flag.Bool("backup", false, "Create .bak before overwriting content")
	textOnly := flag.Bool("textonly", false, "Skip files that appear binary (has NUL byte)")
	maxReadMB := flag.Int("maxreadmb", 1024, "Read files up to this many MB into memory (files larger than this are skipped)")
	includeHidden := flag.Bool("hidden", true, "Include hidden files/folders")
	flag.Parse()

	if *from == "" {
		exitErr("missing -from")
	}
	if *to == "" {
		exitErr("missing -to")
	}

	start := time.Now()

	// Collect all paths.
	var files []string
	var allForRename []string

	err := filepath.WalkDir(*root, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			// Permission or transient error—report and continue.
			fmt.Fprintf(os.Stderr, "warn: skipping %s: %v\n", path, err)
			return nil
		}
		// Respect hidden filter (applies per path segment).
		if !*includeHidden && isHiddenPath(path) && path != *root {
			if d.IsDir() {
				return filepath.SkipDir
			}
			return nil
		}

		// Don’t descend into symlinked directories; treat symlink itself like a file name (for rename).
		if d.Type()&os.ModeSymlink != 0 {
			if *rename && strings.Contains(d.Name(), *from) {
				allForRename = append(allForRename, path)
			}
			return nil
		}

		// Queue content edit for regular files.
		if *edit && d.Type().IsRegular() {
			files = append(files, path)
		}

		// Queue for rename if name matches (files or dirs).
		if *rename && strings.Contains(d.Name(), *from) {
			allForRename = append(allForRename, path)
		}
		return nil
	})
	if err != nil {
		exitErr("walk error: %v", err)
	}

	// Phase 1: content edits
	edited := 0
	skippedLarge := 0
	if *edit {
		edited, skippedLarge = processContents(files, *from, *to, *workers, *dryRun, *backups, *textOnly, int64(*maxReadMB)*1024*1024)
	}

	// Phase 2: rename paths (children first). Sort by depth (desc), then by path length (desc).
	renamed := 0
	if *rename && len(allForRename) > 0 {
		sort.Slice(allForRename, func(i, j int) bool {
			di := depth(allForRename[i])
			dj := depth(allForRename[j])
			if di != dj {
				return di > dj
			}
			return len(allForRename[i]) > len(allForRename[j])
		})
		for _, oldPath := range allForRename {
			base := filepath.Base(oldPath)
			newBase := strings.ReplaceAll(base, *from, *to)
			if base == newBase {
				continue
			}
			newPath := filepath.Join(filepath.Dir(oldPath), newBase)
			if *dryRun {
				fmt.Printf("[rename] %s -> %s\n", oldPath, newPath)
				renamed++
				continue
			}
			// Ensure parent exists (it should), avoid collisions.
			if _, err := os.Lstat(newPath); err == nil {
				fmt.Fprintf(os.Stderr, "warn: target exists, skipping rename: %s -> %s\n", oldPath, newPath)
				continue
			}
			if err := os.Rename(oldPath, newPath); err != nil {
				fmt.Fprintf(os.Stderr, "warn: rename failed: %s -> %s: %v\n", oldPath, newPath, err)
				continue
			}
			fmt.Printf("[renamed] %s -> %s\n", oldPath, newPath)
			renamed++
		}
	}

	elapsed := time.Since(start)
	fmt.Printf("\nDone in %s\n", elapsed)
	if *edit {
		fmt.Printf("Content edits: %d", edited)
		if skippedLarge > 0 {
			fmt.Printf(" (skipped too-large files: %d)", skippedLarge)
		}
		fmt.Println()
	}
	if *rename {
		fmt.Printf("Renames: %d\n", renamed)
	}
}

func processContents(files []string, from, to string, workers int, dry, backups, textOnly bool, maxRead int64) (edited, skippedLarge int) {
	fromB := []byte(from)
	toB := []byte(to)

	jobs := make(chan Job, workers*4)
	var wg sync.WaitGroup

	var mu sync.Mutex
	editedCount := 0
	skippedLargeCount := 0

	worker := func() {
		defer wg.Done()
		for job := range jobs {
			path := job.path
			info, err := os.Lstat(path)
			if err != nil {
				fmt.Fprintf(os.Stderr, "warn: stat failed %s: %v\n", path, err)
				continue
			}
			if !info.Mode().IsRegular() {
				continue
			}
			if info.Size() > maxRead {
				mu.Lock()
				skippedLargeCount++
				mu.Unlock()
				continue
			}

			b, err := os.ReadFile(path)
			if err != nil {
				fmt.Fprintf(os.Stderr, "warn: read failed %s: %v\n", path, err)
				continue
			}

			if textOnly && looksBinary(b) {
				continue
			}

			if !bytes.Contains(b, fromB) {
				continue
			}
			newB := bytes.ReplaceAll(b, fromB, toB)
			if bytes.Equal(b, newB) {
				continue
			}

			if dry {
				fmt.Printf("[edit] %s (would replace %d bytes)\n", path, len(b)-len(newB))
				mu.Lock()
				editedCount++
				mu.Unlock()
				continue
			}

			if backups {
				if err := copyFile(path, path+".bak", info.Mode()); err != nil {
					fmt.Fprintf(os.Stderr, "warn: backup failed %s: %v\n", path, err)
					// continue anyway
				}
			}

			// Preserve perms.
			if err := os.WriteFile(path, newB, info.Mode()); err != nil {
				fmt.Fprintf(os.Stderr, "warn: write failed %s: %v\n", path, err)
				continue
			}
			fmt.Printf("[edited] %s\n", path)
			mu.Lock()
			editedCount++
			mu.Unlock()
		}
	}

	wg.Add(workers)
	for i := 0; i < workers; i++ {
		go worker()
	}
	for _, f := range files {
		jobs <- Job{path: f}
	}
	close(jobs)
	wg.Wait()

	return editedCount, skippedLargeCount
}

func copyFile(src, dst string, mode fs.FileMode) error {
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()
	out, err := os.OpenFile(dst, os.O_CREATE|os.O_TRUNC|os.O_WRONLY, mode)
	if err != nil {
		return err
	}
	defer func() { _ = out.Close() }()
	if _, err := io.Copy(out, in); err != nil {
		return err
	}
	return out.Sync()
}

func looksBinary(b []byte) bool {
	// Simple heuristic: any NUL byte -> binary.
	const sample = 4096
	limit := len(b)
	if limit > sample {
		limit = sample
	}
	for i := 0; i < limit; i++ {
		if b[i] == 0x00 {
			return true
		}
	}
	return false
}

func isHiddenPath(path string) bool {
	// Hidden if any segment starts with "."
	for {
		base := filepath.Base(path)
		if base == "." || base == string(filepath.Separator) {
			return false
		}
		if strings.HasPrefix(base, ".") {
			return true
		}
		dir := filepath.Dir(path)
		if dir == path {
			return false
		}
		path = dir
	}
}

func depth(p string) int {
	n := 0
	for {
		dir := filepath.Dir(p)
		if dir == p {
			break
		}
		n++
		p = dir
	}
	return n
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

func exitErr(fmtStr string, a ...any) {
	fmt.Fprintf(os.Stderr, "error: "+fmtStr+"\n", a...)
	os.Exit(2)
}
