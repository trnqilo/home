#!/usr/bin/env pwsh

function Invoke-Mux {
  $sessionExists = tmux ls 2>$null | Select-String "attached|detached"

  if ($sessionExists) {
    tmux attach
    return
  }

  Write-TmuxConfiguration
  tmux -f (Join-Path $HOME ".tmux.conf")
}

function Write-TmuxConfiguration {
  $accentColor = "white"
  $style = [Environment]::GetEnvironmentVariable("PROMPT_STYLE", "User")

  if (![string]::IsNullOrWhiteSpace($style)) {
    $styleParts = $style -split " "
    if ($styleParts.Count -ge 2) {
      $accentColor = $styleParts[-1].ToLower()
    }
  }

  $targetPath = Join-Path $HOME ".tmux.conf"
  $newConfig = @"
set -g mouse on
set -g history-limit 100000
setw -g mode-keys vi
set-option -g status-bg $accentColor
set-option -g pane-border-style fg=$accentColor
set-option -g pane-active-border-style fg=$accentColor
set-option -g status-right '%m/%d/%y %H:%M'
"@

  $currentContent = ""
  if (Test-Path $targetPath) {
    $currentContent = Get-Content $targetPath -Raw
  }

  if ($currentContent.Trim() -ne $newConfig.Trim()) {
    $newConfig | Out-File -FilePath $targetPath -Encoding ascii -Force
  }
}

Set-Alias mux Invoke-Mux
