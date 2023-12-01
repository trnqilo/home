@echo off
setlocal

set "PS_SCRIPT=%~dp0base64.ps1"

if "%~1"=="" (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%"
) else (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -InputString "%*"
)

endlocal
