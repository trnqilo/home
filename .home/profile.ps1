function prompt {
  if ($Shell) {
    $shellName = $Shell
  } elseif ($env:SHELL) {
    $shellName = Split-Path $env:SHELL -Leaf
  } else {
    $shellName = (Get-Process -Id $PID).ProcessName
  }
  $pColor = if ($env:PROMPT_COLOR) { $env:PROMPT_COLOR } else { "White" }
  $pAccent = if ($env:PROMPT_ACCENT) { $env:PROMPT_ACCENT } else { "White" }
  $pIcon = if ($env:PROMPT_ICON) { $env:PROMPT_ICON } else { ">:" }
  $time = Get-Date -Format "HH:mm:ss"
  $currentPath = $ExecutionContext.SessionState.Path.CurrentLocation.Path
  $displayPath = $currentPath -replace [regex]::Escape($HOME), "~"
  Write-Host "$env:USERNAME " -NoNewline -ForegroundColor $pColor
  Write-Host "$env:COMPUTERNAME " -NoNewline -ForegroundColor $pAccent
  Write-Host "$shellName " -NoNewline -ForegroundColor $pAccent
  Write-Host "$time " -NoNewline -ForegroundColor $pAccent
  Write-Host "$displayPath" -ForegroundColor $pColor
  Write-Host "$pIcon" -NoNewline -ForegroundColor $pColor
  return " "
}

function Stop-Session { exit }

Set-Alias -Name qq -Value Stop-Session
