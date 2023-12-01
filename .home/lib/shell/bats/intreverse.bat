@echo off
setlocal

set "PS_SCRIPT=%~dp0intreverse.ps1"
set "num=%~1"

if "%num%"=="" set /p "num="
if "%num%"=="" exit /b 0

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Number "%num%"

endlocal
