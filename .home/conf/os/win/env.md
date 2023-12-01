+ install msys2
  + https://www.msys2.org/
  + install to `C:\root`
  + add `C:\root\usr\bin` to PATH
  + run `bash -l`

+ packages
  + find `pacman -Ss package_search`
  + install `pacman -S git vim zip unzip`
  + update `pacman -Syu`

+ windows home directory
  + `vim /etc/nsswitch.conf`
  + `db_home: windows`
  + restart bash

+ sdkman
  + `curl -s "https://get.sdkman.io" | bash`
  + find `sdk list java`
  + install `sdk install java 17.0.10-amzn`

+ idea
  + https://www.jetbrains.com/idea/download/
  + add `/c/path/to/idea/bin/` to PATH in `$HOME/.bashrc`
  + restart shell and run `idea64`

+ enable paths longer than 256 characters
  + `reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\FileSystem" -v LongPathsEnabled -t reg_dword -d 1 -f`

+ timezone fix
  + `reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\TimeZoneInformation" -v RealTimeIsUniversal -t reg_dword -d 1 -f`

+ mount EFI / ESP partition

```shell
$ diskpart
DISKPART> select disk 0
DISKPART> select partition 2
DISKPART> assign
$ cd /d/
```

+ utils

```shell
bcdedit /set hypervisorlaunchtype auto # change hyper v launch type 'auto' or 'off'
devmgmt # device manager
diskmgmt # disk paritioning tool
gpedit # group policy
msconfig # boot, services, etc
msinfo32 # system info
ncpa.cpl # network adapters
netplwiz # user manager
powercfg /h off # hibernate disable
regedit # registry editor
secpol # security policy
services.msc # services config
wf # windows firewall
```

+ read sensors

```bat
@echo off
for /f "skip=1 tokens=2 delims==" %%A in ('wmic /namespace:\\root\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature /value') do set /a "cel=(%%~A*10)-27315"
echo %cel:~0,-2%.%cel:~-2%
```

+ remove gamebar

```bat
Get-AppxPackage Microsoft.XboxGamingOverlay | Remove-AppxPackage
REM start "" ms-windows-store://pdp/?productid=9NZKPSTSNW4P
```

+ firewall lockdown

```bat
pause
exit

::netsh advfirewall firewall set rule group="Core Networking" new enable=yes

::flush existing rules
netsh advfirewall firewall delete rule name="TEMP" dir=out
netsh advfirewall firewall delete rule name="TEMP" dir=in
netsh advfirewall firewall delete rule name="AUTORULE" dir=out
netsh advfirewall firewall delete rule name="AUTORULE_IN" dir=in
netsh advfirewall firewall delete rule name="AUTORULE_BLOCK" dir=out
netsh advfirewall firewall delete rule name="AUTORULE_BLOCK" dir=in

@cd /d "C:\Program Files\"
call :FIREWALL_ALLOW_OUT


echo.&pause&goto:eof


:FIREWALL_ALLOW_OUT
@ setlocal enableextensions
for /R %%a in (*.exe) do (
start /b netsh advfirewall firewall add rule name="AUTORULE" dir=out program="%%a" action=allow
start /b netsh advfirewall firewall add rule name="AUTORULE_BLOCK" dir=in program="%%a" action=block
)
goto:eof


:FIREWALL_ALLOW_IN
@ setlocal enableextensions
for /R %%a in (*.exe) do (
start /b netsh advfirewall firewall add rule name="AUTORULE_IN" dir=in program="%%a" action=allow
)
goto:eof

:FIREWALL_BLOCK
@ setlocal enableextensions
for /R %%a in (*.exe) do (
start /b netsh advfirewall firewall add rule name="AUTORULE_BLOCK" dir=out program="%%a" action=block
start /b netsh advfirewall firewall add rule name="AUTORULE_BLOCK" dir=in program="%%a" action=block
)
goto:eof

:FIREWALL_BLOCK_CURRENT_DIR
@ setlocal enableextensions
@ cd /d "%~dp0"
for /R %%a in (*.exe) do (
netsh advfirewall firewall add rule name="AUTORULE_BLOCK" dir=out program="%%a" action=block
)
goto:eof
```

+ wol

```ps1
$Mac = $args[0]
$MacByteArray = $Mac -split "[:-]" | ForEach-Object { [Byte] "0x$_"}
[Byte[]] $MagicPacket = (,0xFF * 6) + ($MacByteArray  * 16)
$UdpClient = New-Object System.Net.Sockets.UdpClient
$UdpClient.Connect(([System.Net.IPAddress]::Broadcast),7)
$UdpClient.Send($MagicPacket,$MagicPacket.Length)
$UdpClient.Close()
```

+ nvidia kp hack

```powershell
rem DXDGI_ERROR_DEVICE_HUNG
reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\GraphicsDrivers" -v "TdrDdiDelay" -t reg_dword -d 60 -f
reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\GraphicsDrivers" -v "TdrDelay" -t reg_dword -d 60 -f
```

+ wsl2

```bat
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
wsl --set-default-version 2
wsl --update --web-download
wsl --install -d Ubuntu-22.04 --web-download
```
