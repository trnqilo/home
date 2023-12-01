@echo off
setlocal

if "%~2" == "" (
  if "%~1" == "" (
    echo Usage: iddqd [username] [optional_command]
    exit /b 1
  )
  echo [!] Opening styled shell for %~1...
  runas /savecred /user:%~1 "cmd /c powsh"
  goto :eof
)

set "USER_FLAG=%~1"
shift
set "COMMAND=%*"

echo [!] Executing as %USER_FLAG%: %COMMAND%
runas /savecred /user:%USER_FLAG% "cmd /c %COMMAND%"

:eof
endlocal
exit /b
