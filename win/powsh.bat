@echo off
setlocal

for /f "delims=" %%i in ('powershell -NoProfile -Command "$PROFILE"') do set "PS_PROFILE=%%i"

for %%f in ("%PS_PROFILE%") do if not exist "%%~dpf" mkdir "%%~dpf"

set "REMOTE_URL=https://raw.githubusercontent.com/trnqilo/home/refs/heads/home/profile.ps1"

set "FETCH=0"
if /i "%~1"=="-x" (
  set "FETCH=1"
  shift
)
if not exist "%PS_PROFILE%" set "FETCH=1"

if "%FETCH%"=="1" (
  echo [!] Fetching profile from GitHub to: %PS_PROFILE%
  (curl -sL "%REMOTE_URL%" -o "%PS_PROFILE%") 2>nul
  if %errorlevel% neq 0 (
    echo [!] Warning: Could not fetch profile.
  )
)

set "LOAD_CMD=. '%PS_PROFILE%'"

if /i "%~x1"==".ps1" (
  powershell -ExecutionPolicy Bypass -File "%~1"
  goto :eof
)

if not "%~1"=="" (
  powershell -NoExit -ExecutionPolicy Bypass -Command "%LOAD_CMD%; %*"
) else (
  powershell -NoExit -ExecutionPolicy Bypass -Command "%LOAD_CMD%"
)

:eof
endlocal
