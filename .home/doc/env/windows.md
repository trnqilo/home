## install msys2 with packages

+ download msys2 from https://www.msys2.org/
+ install to desired location: eg. `C:\msys2`
+ update packages `pacman -Syu`
+ install packages `pacman -S git vim zip unzip`
  + find additional packages `pacman -Ss _search_terms_`
+ configure home directory `vim /etc/nsswitch.conf`
  + change db_home to windows: `db_home: windows`

## install dev tools

+ install sdkman `curl -s "https://get.sdkman.io" | bash`
+ install java: eg. `sdk install java 17.0.10-amzn`
  + find candidates `sdk list java`
+ install idea https://www.jetbrains.com/idea/download/
+ add `/c/path/to/idea/bin/` to PATH in `$HOME/.bashrc`
+ restart shell and run `idea64`

## run bash from anywhere

+ open environment variables for Windows
  + edit `Path` variable for `System`
    + add `C:\msys2\usr\bin` to the top
      + eg. `C:\msys2\usr\bin;C:\Windows\system32...`
  + open cmd or powershell and run `bash -l`

## enable paths longer than 256 characters

+ run from any shell as admin:
  + `reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\FileSystem" -v LongPathsEnabled -t reg_dword -d 1 -f`

## timezone fix

+ run from any shell as admin:
  + `reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\TimeZoneInformation" -v RealTimeIsUniversal -t reg_dword -d 1 -f`
