param([string]$CmdName)

$c = Get-Command $CmdName -ErrorAction SilentlyContinue

if ($c) {
  if ($c.CommandType -eq 'Alias') {
    Write-Host "[Alias]: " -NoNewline -ForegroundColor Cyan
    Write-Host $c.Definition
    exit 0
  }
  if ($c.CommandType -eq 'Function') {
    Write-Host "[Function]:" -ForegroundColor Cyan
    Write-Host $c.ScriptBlock
    exit 0
  }
}

$files = where.exe $CmdName "$CmdName.bat" "$CmdName.ps1" 2>$null
if ($files) {
  $target = $files[0]
  Write-Host "[File]: $target" -ForegroundColor Cyan
  Write-Host ("-" * 60)

  $raw = Get-Content -Path $target -Encoding Byte -TotalCount 1024 -ErrorAction SilentlyContinue
  if ($raw -contains 0) {
    Write-Host "[Binary File] - Cannot display source." -ForegroundColor Gray
  } else {
    Get-Content -Path $target | ForEach-Object {
      if ($_ -match '^(rem|::|#|//)') { Write-Host $_ -ForegroundColor Green }
      elseif ($_ -match '\b(set|if|else|goto|call|exit|function|return|param)\b') { Write-Host $_ -ForegroundColor Yellow }
      else { Write-Host $_ }
    }
  }
  exit 0
}

Write-Host "[!] Could not find definition for: $CmdName" -ForegroundColor Red
exit 1
