$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$SubDirectory = "ff"
$ExeName      = "firefox.exe"
$ExePath = Join-Path $ScriptDir "$SubDirectory\$ExeName"
if (Test-Path $ExePath) {
  Start-Process -FilePath $ExePath -WorkingDirectory (Split-Path $ExePath)
} else {
  Write-Error "Not found: $ExePath"
  exit 1
}
