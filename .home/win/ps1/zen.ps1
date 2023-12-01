function Zen {
  $processes = @(
    'explorer', 'StartMenuExperienceHost', 'ShellExperienceHost', 'SearchHost', 'TextInputHost',
    'Widgets', 'WidgetService', 'Copilot', 'YourPhone', 'PhoneExperienceHost', 'Teams', 'MSTeams',
    'GameBar', 'GameBarFTServer', 'GameBarPresenceWriter', 'XboxPcApp', 'XboxGameBarWidgets',
    'OneDrive', 'msedge', 'msedgewebview2', 'MicrosoftEdgeUpdate'
  )

  try {
    $shellScript = 'Set-Location -LiteralPath $HOME; $rc = Join-Path $env:USERPROFILE ''Scripts\pwshrc.ps1''; if (Test-Path -LiteralPath $rc) { . $rc }'
    $shellCommand = '"' + (Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe') + '" -NoLogo -NoProfile -NoExit -ExecutionPolicy Bypass -Command "' + $shellScript + '"'

    foreach ($name in $processes) {
      Get-Process -Name $name -ErrorAction SilentlyContinue |
        Stop-Process -Force -ErrorAction SilentlyContinue
    }

    Metro-Stop

    Set-ItemProperty `
      -Path 'HKCU:\Software\Microsoft\Windows NT\CurrentVersion\Winlogon' `
      -Name 'Shell' `
      -Type String `
      -Value $shellCommand

    Set-ItemProperty `
      -Path 'HKCU:\Software\Microsoft\Windows NT\CurrentVersion\Winlogon' `
      -Name 'AutoRestartShell' `
      -Type DWord `
      -Value 1

    Write-Host 'Tranquilo 🧘' -ForegroundColor Cyan
  }
  catch {
    Write-Error "Error: $($_.Exception.Message)"
    exit 1
  }
}

function Unzen {
  try {
    Set-ItemProperty `
      -Path 'HKCU:\Software\Microsoft\Windows NT\CurrentVersion\Winlogon' `
      -Name 'AutoRestartShell' `
      -Type DWord `
      -Value 1 `
      -ErrorAction SilentlyContinue

    if (-not (Get-Process explorer -ErrorAction SilentlyContinue)) {
      Start-Process explorer.exe
    }
    Set-ItemProperty `
      -Path 'HKCU:\Software\Microsoft\Windows NT\CurrentVersion\Winlogon' `
      -Name 'Shell' `
      -Type String `
      -Value 'explorer.exe'

    Write-Host 'Bárbaro! 🚀' -ForegroundColor Green
  }
  catch {
    Write-Error "Error: $($_.Exception.Message)"
    exit 1
  }
}

function Metro {
  param([string]$Page = "")
  Start-Process "ShellAppRuntime.exe"
  Start-Sleep -Milliseconds 1234
  Start-Process "ms-settings:$Page"
  Write-Host "Metro ⚙️"
}

function Metro-Stop {
  Stop-Process -Name "SystemSettings" -Force -ErrorAction SilentlyContinue
  Start-Sleep -Milliseconds 1234
  Stop-Process -Name "ShellAppRuntime" -Force -ErrorAction SilentlyContinue
}

if ($args.Count -gt 0 -and $args[0].ToLower() -eq 'off') { Unzen }
elseif ($args.Count -gt 0 -and $args[0].ToLower() -eq 'metro') { Metro $args[1] }
else { Zen }
