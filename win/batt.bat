@echo off
for /f "usebackq" %%A in (`powershell -NoProfile -Command "(Get-CimInstance -ClassName Win32_Battery).EstimatedChargeRemaining"`) do set "battery=%%A"

echo %battery%%%
