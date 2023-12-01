
:custom
echo [?] This will overwrite custom assets.
set /p "sure=Are you sure? (y/n): "
if /i "%sure%" neq "y" goto :eof

set "cstrike_path=%STEAM_APPS_COMMON%\Counter-Strike Source\cstrike"
set "encoded_file=Source\cstrike"

if not exist "%encoded_file%" (
    echo [!] Encoded assets not found at %encoded_file%
    exit /b 1
)

echo [+] Decoding Base64 assets...
if exist "%cstrike_path%\custom" rd /s /q "%cstrike_path%\custom"

:: certutil is our Windows 'base64 -d'
certutil -decode "%encoded_file%" "%TEMP%\cstrike.tar.gz" >nul

echo [+] Extracting Tarball...
pushd "%cstrike_path%"
tar -zxvf "%TEMP%\cstrike.tar.gz"
popd

del "%TEMP%\cstrike.tar.gz"
echo [!] Custom assets deployed.
goto :eof

:: --- ERRORS & EXIT ---
:steam_error
echo [!] STEAM_HOME is invalid or not set.
exit /b 1

:eof
exit /b
