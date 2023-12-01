@echo off
setlocal

set "action=%~1"
if /i "%action%"=="i" set "action=install"
set "target=%~2"

if "%action%"=="" (
  echo Usage: pkg [install|i|anything] [name]
  exit /b 0
)

if /i "%action%"=="install" (
  if "%target%"=="" (
    echo [!] Please specify a package to install.
    exit /b 1
  )

  if /i "%target%"=="wsl2" (
    echo [+] Setting up WSL2...
    wudo wsl --install
  ) else (
    echo [+] '%target%' not a macro. Searching winget...
::   winget install --id Microsoft.VisualStudio.2022.Community --source winget
    winget install --name "%target%" --source winget
  )
  exit /b %errorlevel%
)

winget %action% %target%

endlocal
