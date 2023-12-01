@echo off
setlocal enabledelayedexpansion

set "PS_SCRIPT=%~dp0httpserv.ps1"
set "OPEN_BROWSER=false"

if /i "%~1"=="o" (set "OPEN_BROWSER=true" & shift)
if /i "%~1"=="open" (set "OPEN_BROWSER=true" & shift)

set "TARGET_DIR=%cd%"
if not "%~1"=="" if exist "%~1" (set "TARGET_DIR=%~f1")

set "PORT=%~2"
if "%PORT%"=="" set "PORT=1234"

if %PORT% LSS 1024 (
  net session >nul 2>&1
  if %errorLevel% neq 0 (
    echo [!] Port %PORT% requires Admin. Elevating...
    powershell -Command "Start-Process cmd -ArgumentList '/c %~f0 %*' -Verb RunAs"
    exit /b
  )
)

if "%OPEN_BROWSER%"=="true" (
  start "" "http://localhost:%PORT%"
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" -Port %PORT% -BaseDir "%TARGET_DIR%"

endlocal
