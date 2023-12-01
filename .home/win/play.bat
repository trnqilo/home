@echo off
setlocal enabledelayedexpansion

set "game=%~1"
if "%game%"=="" goto :help

if /i "%game%"=="install" goto :installer_menu

for %%a in (cs cs2 csgo)       do if /i "%game%"=="%%a" (set "id=730"      & goto :steam)
for %%a in (css cstrike)       do if /i "%game%"=="%%a" (set "id=240"      & goto :steam)
for %%a in (apex)              do if /i "%game%"=="%%a" (set "id=1172470"  & goto :steam)
for %%a in (tf2 tf)            do if /i "%game%"=="%%a" (set "id=440"      & goto :steam)
for %%a in (ow ow2 overwatch)  do if /i "%game%"=="%%a" (set "id=2357570"  & goto :steam)
for %%a in (rl rocketleague)   do if /i "%game%"=="%%a" (set "id=252950"   & goto :steam)
for %%a in (l4d2)              do if /i "%game%"=="%%a" (set "id=550"      & goto :steam)
for %%a in (assetto acor)      do if /i "%game%"=="%%a" (set "id=244210"   & goto :steam)
for %%a in (gmod)              do if /i "%game%"=="%%a" (set "id=4000"     & goto :steam)
for %%a in (blend blender)     do if /i "%game%"=="%%a" (set "id=365670"   & goto :steam)
for %%a in (ins insurgency)    do if /i "%game%"=="%%a" (set "id=222880"   & goto :steam)
for %%a in (portal)            do if /i "%game%"=="%%a" (set "id=400"      & goto :steam)
for %%a in (portal2)           do if /i "%game%"=="%%a" (set "id=620"      & goto :steam)
for %%a in (pubg)              do if /i "%game%"=="%%a" (set "id=578080"   & goto :steam)
for %%a in (rust)              do if /i "%game%"=="%%a" (set "id=252490"   & goto :steam)
for %%a in (rss siege)         do if /i "%game%"=="%%a" (set "id=359550"   & goto :steam)
for %%a in (fn fortnite)       do if /i "%game%"=="%%a" (set "id=Fortnite" & goto :epic)
for %%a in (val valorant)      do if /i "%game%"=="%%a" (set "id=valorant" & goto :riot)

if /i "%game%"=="steam"  goto :steam
if /i "%game%"=="epic"   goto :epic
if /i "%game%"=="riot"   goto :riot

echo [!] Unknown game or launcher: %game%
exit /b


:steam
if defined id (
  echo [STEAM] Launching ID: %id%...
  start "" "steam://rungameid/%id%"
) else (
  echo [STEAM] Opening Library...
  start "" "steam://open/games"
)
exit /b


:epic
if not defined EPIC_HOME (
  echo [!] 'EPIC_HOME' is not set.
  exit /b 1
)
set "epic_exe=%EPIC_HOME%Launcher\Portal\Binaries\Win64\EpicGamesLauncher.exe"
if defined id (
  echo [EPIC] Launching %id%...
  if exist "%epic_exe%" (
    start "" "%epic_exe%" "com.epicgames.launcher://apps/%id?action=launch"
  ) else (
    start "" "com.epicgames.launcher://apps/%id?action=launch"
  )
) else (
  echo [EPIC] Opening Launcher...
  start "" "%epic_exe%"
)
exit /b


:riot
if not defined RIOT_HOME (
  echo [!] 'RIOT_HOME' is not set.
  exit /b 1
)
if defined id (
  set "riot_exec=%riot_home%\Riot Client\RiotClientServices.exe"
  echo [RIOT] Launching %id%...
  if exist "%riot_exec%" (
    start "" "%riot_exec%" --launch-product=%id% --launch-patchline=live
  ) else (
    echo [!] Riot Client not found at %riot_exec%
  )
) else (
  set "riot_ux=%riot_home%\Riot Client\UX\RiotClientUx.exe"
  echo [RIOT] Opening Dashboard...
  if exist "%riot_ux%" ( start "" "%riot_ux%" ) else ( start "" "%riot_exec%" )
)
exit /b


:installer_menu
echo.
echo 1. Steam
echo 2. Epic
echo 3. Riot
echo.
set /p choice="Select an app to install (q to quit): "

set "STEAM_URL=https://repo.steampowered.com/windows/SteamSetup.exe"
set "EPIC_URL=https://launcher-public-service-prod06.ol.epicgames.com/launcher/api/installer/download/EpicGamesLauncherInstaller.msi"
set "RIOT_URL=https://static.developer.riotgames.com/installer/RiotClientInstalls.exe"

if "%choice%"=="1" (set "target=%STEAM_URL%" & set "name=SteamSetup.exe" & set "args=/S" & goto :do_install)
if "%choice%"=="2" (set "target=%EPIC_URL%"  & set "name=EpicInstaller.msi" & set "args=/passive" & goto :do_install)
if "%choice%"=="3" (set "target=%RIOT_URL%"  & set "name=RiotInstaller.exe" & set "args=" & goto :do_install)
if /i "%choice%"=="q" exit /b
goto :installer_menu


:do_install
echo [+] Downloading %name% to HOME...
curl -L -o "%HOME%\%name%" "%target%"
echo [+] Launching installer...
if "%name:~-3%"=="msi" ( start "" msiexec /i "%HOME%\%name%" %args% ) else ( start "" "%HOME%\%name%" %args% )
exit /b


:help
echo Usage: play [ cs | val | fn | etc ]
echo        play [ steam | riot | epic ]
echo        play install
exit /b
