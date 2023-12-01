@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0lspci.ps1"
endlocal
