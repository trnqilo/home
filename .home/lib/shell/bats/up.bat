@echo off
setlocal

set "PS_SCRIPT=%~dp0up.ps1"
powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%"

endlocal
