@echo off
setlocal

set "PS_SCRIPT=%~dp0wakeonlan.ps1"
set "MAC=%~1"

if "%MAC%"=="" (
  echo Usage: wakeonlan [MAC_ADDRESS]
  echo Example: wakeonlan 00:11:22:33:44:55
  exit /b 1
)

echo [!] Preparing Magic Packet...
powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -MacAddress "%MAC%"

endlocal
