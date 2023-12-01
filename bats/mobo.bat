@echo off
setlocal

echo Checking Motherboard...

:: We call PowerShell and use 'Select-Object -ExpandProperty' to get raw text
for /f "usebackq tokens=*" %%A in (`powershell -NoProfile -Command "Get-CimInstance Win32_BaseBoard | Select-Object -ExpandProperty Product"`) do set "MOBO_MODEL=%%A"
for /f "usebackq tokens=*" %%A in (`powershell -NoProfile -Command "Get-CimInstance Win32_BaseBoard | Select-Object -ExpandProperty Manufacturer"`) do set "MOBO_VENDOR=%%A"

echo Manufacturer : %MOBO_VENDOR% %MOBO_MODEL%

pause
