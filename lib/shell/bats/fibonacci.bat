@echo off
setlocal

set "PS_SCRIPT=%~dp0fibonacci.ps1"

if "%~1"=="" (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Range "10"
) else (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Range "%~1"
)

endlocal
