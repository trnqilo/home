@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0lsusb.ps1"
endlocal
