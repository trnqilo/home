@echo off
setlocal enabledelayedexpansion

for /f "tokens=2*" %%A in ('reg query "HKCU\Software\Valve\Steam" /v "SteamPath" 2^>nul') do set "STEAM_HOME=%%B"
if not defined STEAM_HOME set "STEAM_HOME=C:\Program Files (x86)\Steam"

set "STEAM_HOME=%STEAM_HOME:/=\%"
set "APPS_COMMON=%STEAM_HOME%\steamapps\common"

echo --- Syncing Game Configurations ---

set "games="Counter-Strike Source|cstrike" "Half-Life 2|hl2" "insurgency2|insurgency" "Left 4 Dead 2|left4dead2" "Portal|portal" "Portal 2|portal2" "Team Fortress 2|tf""

for %%G in (%games%) do (
  for /f "tokens=1,2 delims=|" %%a in (%%G) do (
    set "game_folder=%%~a"
    set "cfg_subpath=%%~b"
    set "full_dest=%APPS_COMMON%\!game_folder!\!cfg_subpath!\cfg"

    if exist "%APPS_COMMON%\!game_folder!" (
      echo [+] Configuring !game_folder!...
      copy /y ".\Source\source.cfg" "!full_dest!\source.cfg" >nul
      copy /y ".\Source\joy.cfg"  "!full_dest!\joy.cfg"  >nul
    )
  )
)

set "cs2_cfg=%APPS_COMMON%\Counter-Strike Global Offensive\game\csgo\cfg"
if exist "%APPS_COMMON%\Counter-Strike Global Offensive" (
  echo [+] Configuring Counter-Strike 2...
  copy /y ".\Source2\source2.cfg" "%cs2_cfg%\source2.cfg" >nul
)

echo [+] Syncing bulk game folders...
xcopy /s /e /y /q ".\Apex Legends"  "%APPS_COMMON%\Apex Legends\"
xcopy /s /e /y /q ".\Half-Life"     "%APPS_COMMON%\Half-Life\"
xcopy /s /e /y /q ".\Quake 3 Arena" "%APPS_COMMON%\Quake 3 Arena\"
xcopy /s /e /y /q ".\Rust"          "%APPS_COMMON%\Rust\"

echo --- Sync Complete! ---
pause
