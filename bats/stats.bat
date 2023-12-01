@echo off
setlocal

set "PS_SCRIPT=%~dp0stats.ps1"

net session >nul 2>&1
if %errorLevel% == 0 (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Elevated $true
) else (
  wudo powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Elevated $true
)

endlocal
