@echo off
setlocal

if "%~3"=="" (
  echo Usage: shtyle [Icon] [Color] [Accent]
  echo Example: shtyle "$ " Cyan DarkGray
  echo.
  echo Colors: Black, Blue, Cyan, Gray, Green, Magenta, Red, White, Yellow
  exit /b
)

set "PROMPT_ICON=%~1"
set "PROMPT_COLOR=%~2"
set "PROMPT_ACCENT=%~3"

setx PROMPT_ICON "%~1" >nul
setx PROMPT_COLOR "%~2" >nul
setx PROMPT_ACCENT "%~3" >nul

endlocal & (
  set "PROMPT_ICON=%~1"
  set "PROMPT_COLOR=%~2"
  set "PROMPT_ACCENT=%~3"
  echo [+] Shtyle updated! (Icon: %~1, Color: %~2, Accent: %~3^)
)
