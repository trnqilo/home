@echo off
setlocal

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0whatsmyip.ps1"

endlocal
