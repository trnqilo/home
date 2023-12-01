@echo off
setlocal

set "PS_SCRIPT=%~dp0why.ps1"

if "%~1"==" " (
  echo Usage: why [command]
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -CmdName "%~1"

endlocal
