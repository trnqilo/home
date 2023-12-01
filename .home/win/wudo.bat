@echo off
setlocal

set "PAUSE= & pause"
if /i "%~1"=="-x" (
  set "PAUSE="
  shift
)

net session >nul 2>&1

if %errorLevel% == 0 (
  %*
) else (
  powershell -Command "Start-Process cmd -ArgumentList '/c %* %PAUSE%' -Verb RunAs"
)

endlocal
