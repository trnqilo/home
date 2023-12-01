
export packages='gcc procps-ng diffutils unzip zip mingw-w64-x86_64-gnuplot'
export PROGRAMFILESX86="$PROGRAMFILES\..\Program\ Files\ \(x86\)"
export PSH_HISTORY="$APPDATA/Microsoft/Windows/PowerShell/PSReadLine/ConsoleHost_history.txt"

alias slk='secretly "$LOCALAPPDATA/slack/slack.exe"'
alias winvars='rundll32 sysdm.cpl,EditEnvironmentVariables'
alias hypervinit='bcdedit -set hypervisorlaunchtype auto'
alias ip='ipcmd="ipconfig.exe -all" ipcmd'

include "$HOME/.exe"
