@echo off
setlocal

set "STASH_ROOT=%LOCALAPPDATA%"
set "label=%~1"

if "%label%"=="" (
  echo Usage: valstash [label] ^| [label] pop
  echo.
  echo Current Riot/Val stashes in Local AppData:
  if exist "%LOCALAPPDATA%\.stash" dir /b "%LOCALAPPDATA%\.stash"
  exit /b
)

if /i "%~2"=="pop" (
  echo [VAL-STASH] Restoring VALORANT and Riot Games from stash: %label%...
  call stash "%label%" pop
) else (
  echo [VAL-STASH] Stashing VALORANT and Riot Games into: %label%...
  call stash "%label%" "VALORANT" "Riot Games"
)

exit /b
