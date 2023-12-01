@echo off
setlocal

set "PS_SCRIPT=%~dp0proportion.ps1"

if "%~3"=="" (
  echo Usage: proportion [val1][unit1] [val2][unit2] [val3][unit3]
  echo Example: proportion 100km 2h 500km
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Val1 "%~1" -Val2 "%~2" -Val3 "%~3"

endlocal
