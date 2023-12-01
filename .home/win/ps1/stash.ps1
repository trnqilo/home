function Get-StashRoot {
  if ($env:STASH_ROOT -and (Test-Path $env:STASH_ROOT)) {
    return $env:STASH_ROOT
  }
  return $PWD
}

function Get-StashDirectory {
  param($Root)
  return Join-Path $Root "_STASH_"
}

function Show-StashHelp {
  Write-Host "Usage:   stash [label] [files...] or [label] pop" -ForegroundColor Yellow
}

function List-Stashes {
  param($StashFolder)
  if (-not (Test-Path $StashFolder)) {
    Write-Host "No stashes found." -ForegroundColor Gray
    return
  }
  Write-Host "[ Stash Directory: $StashFolder ]" -ForegroundColor Cyan
  Get-ChildItem -Path $StashFolder -Directory | Select-Object -ExpandProperty Name
}

function Test-FolderLocked {
  param($FolderPath)
  $lockedFiles = @()
  Get-ChildItem -Path $FolderPath -Recurse -File -ErrorAction SilentlyContinue | ForEach-Object {
    try {
      $stream = [System.IO.File]::Open($_.FullName, 'Open', 'ReadWrite', 'None')
      $stream.Close()
      $stream.Dispose()
    } catch {
      $lockedFiles += $_.FullName
    }
  }
  return $lockedFiles
}

function Pop-Stash {
  param($Label, $StashFolder, $CurrentRoot)
  $targetDir = Join-Path $StashFolder $Label
  if (-not (Test-Path $targetDir)) {
    Write-Host "[!] Stash '$Label' not found." -ForegroundColor Red
    return
  }

  Write-Host "[~] Checking stash '$Label' for locked files before restore..." -ForegroundColor Gray
  $allLocked = @()
  Get-ChildItem -Path $targetDir -Directory | ForEach-Object {
    $locked = Test-FolderLocked -FolderPath $_.FullName
    $allLocked += $locked
  }
  if ($allLocked.Count -gt 0) {
    Write-Host "[!] Stash '$Label' is locked — restore aborted. No files were moved." -ForegroundColor Red
    Write-Host "    Locked files:" -ForegroundColor Red
    $allLocked | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkRed }
    return
  }

  # Also check destination folders that would be overwritten
  Get-ChildItem -Path $targetDir | ForEach-Object {
    $destination = Join-Path $CurrentRoot $_.Name
    if ((Test-Path $destination) -and (Test-Path $destination -PathType Container)) {
      $locked = Test-FolderLocked -FolderPath $destination
      $allLocked += $locked
    }
  }
  if ($allLocked.Count -gt 0) {
    Write-Host "[!] Destination folder(s) are locked — restore aborted. No files were moved." -ForegroundColor Red
    Write-Host "    Locked files:" -ForegroundColor Red
    $allLocked | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkRed }
    return
  }

  Write-Host "[!] Popping stash: $Label" -ForegroundColor Cyan
  Get-ChildItem -Path $targetDir | ForEach-Object {
    $destination = Join-Path $CurrentRoot $_.Name
    if (Test-Path $destination) {
      $confirm = Read-Host "[?] Overwrite $($_.Name)? (y/n)"
      if ($confirm -eq 'y') {
        Remove-Item -Path $destination -Recurse -Force
        Move-Item -Path $_.FullName -Destination $CurrentRoot -Force
      }
    } else {
      Move-Item -Path $_.FullName -Destination $CurrentRoot -Force
    }
  }
  Remove-Item -Path $targetDir -Force -ErrorAction SilentlyContinue
}

function Create-Stash {
  param($Label, $Files, $StashFolder)
  $targetDir = Join-Path $StashFolder $Label
  $root = Get-StashRoot

  # Lock-check all source folders before touching anything
  Write-Host "[~] Checking for locked files before stashing..." -ForegroundColor Gray
  $allLocked = @()
  foreach ($file in $Files) {
    $fullSourcePath = Join-Path $root $file
    if ((Test-Path $fullSourcePath) -and (Test-Path $fullSourcePath -PathType Container)) {
      $locked = Test-FolderLocked -FolderPath $fullSourcePath
      $allLocked += $locked
    }
  }
  if ($allLocked.Count -gt 0) {
    Write-Host "[!] Stash aborted — locked files detected. Nothing was moved." -ForegroundColor Red
    Write-Host "    Locked files:" -ForegroundColor Red
    $allLocked | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkRed }
    return
  }

  if (!(Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
  }
  foreach ($file in $Files) {
    $fullSourcePath = Join-Path $root $file
    if (Test-Path $fullSourcePath) {
      Write-Host "[>] Stashing: $file" -ForegroundColor Green
      Move-Item -Path $fullSourcePath -Destination $targetDir -Force
    } else {
      Write-Host "[!] Skipping: $file (Not found at $fullSourcePath)" -ForegroundColor Yellow
    }
  }
}

function Invoke-StashManager {
  param($Inputs)
  $root = Get-StashRoot
  $stashFolder = Get-StashDirectory -Root $root
  if ($Inputs.Count -eq 0) { Show-StashHelp; return }
  if ($Inputs[0] -eq "list") { List-Stashes -StashFolder $stashFolder; return }
  $label = $Inputs[0]
  if ($Inputs.Count -ge 2 -and $Inputs[1] -eq "pop") {
    Pop-Stash -Label $label -StashFolder $stashFolder -CurrentRoot $root
  } else {
    $files = $Inputs[1..($Inputs.Count - 1)]
    Create-Stash -Label $label -Files $files -StashFolder $stashFolder
  }
}

Invoke-StashManager -Inputs $args
