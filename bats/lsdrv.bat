@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0lsdrv.ps1"
endlocal
