@echo off
setlocal enabledelayedexpansion

set "game=%~1"
if "%game%"=="" goto :help

for %%a in (steam st)          do if /i "%game%"=="%%a" (                    goto :steam)
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
for %%a in (epic egl)          do if /i "%game%"=="%%a" (                    goto :epic )
for %%a in (fn fortnite)       do if /i "%game%"=="%%a" (set "id=Fortnite" & goto :epic )
for %%a in (riot rgl)          do if /i "%game%"=="%%a" (set                 goto :riot )
for %%a in (val valorant)      do if /i "%game%"=="%%a" (set "id=valorant" & goto :riot )

if /i "%game%"=="steam"  goto :steam
if /i "%game%"=="epic"   goto :epic
if /i "%game%"=="riot"   goto :riot

echo [!] Unknown game or launcher: %game%
exit /b

:steam
if defined id (
  set "cmd=start """" ""steam://rungameid/%id%"""
) else (
  set "cmd=start """" ""steam://open/games"""
)
call iddqd steam %cmd%
exit /b

:epic
if not defined EPIC_HOME (echo [!] 'EPIC_HOME' not set. & exit /b 1)
set "epic_exe=%EPIC_HOME%Launcher\Portal\Binaries\Win64\EpicGamesLauncher.exe"
if defined id (
  set "cmd=start """" ""%epic_exe%"" ""com.epicgames.launcher://apps/%id%?action=launch"""
) else (
  set "cmd=start """" ""%epic_exe%""""
)
call iddqd epic %cmd%
exit /b

:riot
if not defined RIOT_HOME (echo [!] 'RIOT_HOME' not set. & exit /b 1)
set "riot_exec=%riot_home%\Riot Client\RiotClientServices.exe"
if defined id (
  set "cmd=start """" ""%riot_exec%"" --launch-product=%id% --launch-patchline=live"
) else (
  set "riot_ux=%riot_home%\Riot Client\UX\RiotClientUx.exe"
  set "cmd=start """" ""!riot_ux!""""
)
call iddqd riot %cmd%
exit /b

:help
echo Usage: play [ alias ]
echo Identities: steam, epic, riot
exit /b
