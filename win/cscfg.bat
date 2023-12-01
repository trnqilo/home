@echo off
setlocal enabledelayedexpansion

for /f "tokens=2*" %%A in ('reg query "HKCU\Software\Valve\Steam" /v "SteamPath" 2^>nul') do set "STEAM_HOME=%%B"
if not defined STEAM_HOME set "STEAM_HOME=%ProgramFiles%\Steam"
set "STEAM_HOME=%STEAM_HOME:/=\%"

set "CS2_CFG_DIR=%STEAM_HOME%\steamapps\common\Counter-Strike Global Offensive\game\csgo\cfg"

set "URL_AUTOEXEC=https://raw.githubusercontent.com/trnqilo/home/refs/heads/lib/play/Counter-Strike%%20Global%%20Offensive/game/csgo/cfg/autoexec.cfg"
set "URL_SOURCE2=https://raw.githubusercontent.com/trnqilo/home/refs/heads/lib/play/Source2/source2.cfg"

echo [+] Target: %CS2_CFG_DIR%

if not exist "%CS2_CFG_DIR%" (
  echo [!] Error: CS2 Config directory not found.
  exit /b 1
)

echo [+] Downloading autoexec.cfg...
curl -L -s -o "%CS2_CFG_DIR%\autoexec.cfg" "%URL_AUTOEXEC%"

echo [+] Downloading source2.cfg...
curl -L -s -o "%CS2_CFG_DIR%\source2.cfg" "%URL_SOURCE2%"

echo [+] Sync Complete!
