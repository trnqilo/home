if [[ -f "$HOME/.profo" ]]; then source "$HOME/.profo"; fi
export DOTHOME="${DOTHOME:-$HOME/.home}" HOLO="${HOLO:-$HOME/.local/home}" CONS="${CONS:-$HOME/.local/home/context}"
export HOMELIB="${HOMELIB:-$HOME/Library}" PROJECTS="${PROJECTS:-$HOME/Projects}"
export ANDROID_HOME="${ANDROID_HOME:-$HOMELIB/android/sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME" ANDROID_NDK_HOME="$ANDROID_HOME/ndk" ANDROID_NDK_ROOT="$ANDROID_HOME/ndk"
export GOROOT="$HOMELIB/go" SWIFT_HOME="$HOMELIB/swift/usr" SDKMAN_DIR="$HOME/.sdkman"

function include { while [[ "$1" ]]; do if [[ -d "$1" ]]; then export PATH="$1:$PATH"; fi; shift; done; }
function import { while [[ "$1" ]]; do if [[ -s "$1" ]]; then source "$1"; fi; shift; done; }
function __init_home {
  OS="${OSTYPE:-`uname -s | awk '{print tolower($1)}'`}"
  if [[ "$OS" == 'darwin'* ]]; then export whereami='darwin'
  elif [[ "$OS" == 'freebsd'* ]]; then export whereami='freebsd'
  elif [[ "$OS" == *'android'* ]]; then export whereami='android'
  elif [[ "$OS" == 'openbsd'* ]]; then export whereami='openbsd'
  elif [[ "$OS" == 'netbsd'* ]]; then export whereami='netbsd'
  elif [[ "$OS" == 'msys'* || "$OS" == 'cygwin' ]]; then export whereami='windows'
  elif [[ "$OS" == 'linux'* ]]; then export whereami='linux'
  else export whereami='unknown'; fi

  if (! test -f "$HOME/.local/bin/bat" && type batcat)&>/dev/null; then
    bash -c 'mkdir -p "$HOME/.local/bin" && ln -s "`type -p batcat`" "$HOME/.local/bin/bat"'
  fi

  BIN="$DOTHOME/bin"
  BIN_OS="$BIN/os/$whereami" BIN_DEV="$BIN/dev" BIN_SH="$BIN/shell"
  export PATH="$BIN/home:$BIN/home/key:$BIN/calc:$BIN/net:$BIN/os:$BIN/sys:$PATH"
  export PATH="$BIN_SH:$BIN_SH/file:$BIN_SH/string:$BIN_SH/run:$PATH"
  export PATH="$BIN_DEV:$BIN/app:$BIN_OS:$PATH"
  unset BIN BIN_OS OS

  include \
    "$ANDROID_HOME/emulator" "$ANDROID_HOME/cmdline-tools/latest/bin" \
    "$ANDROID_HOME/tools" "$ANDROID_HOME/tools/bin" "$ANDROID_HOME/platform-tools" \
    "$HOMELIB/dart-sdk/bin" "$HOMELIB/python" "$HOMELIB/kotlin-native/bin" "$GOROOT/bin" \
    "$HOME/.cargo/bin" "$HOME/.pub-cache/bin" "$SWIFT_HOME/bin" "$HOMELIB/flutter/bin" "$HOMELIB/code/bin" \
    "$HOLO/bin" "$HOME/.local/bin"

  import "$DOTHOME/lib/profile" "$HOLO/profile" "$HOME/.local/profile" \
    "$HOME/.cargo/env" "$SDKMAN_DIR/bin/sdkman-init.sh"

  if [[ -d "$HOME/.local/profiles/" ]]; then import "$HOME/.local/profiles/"*; fi
};__init_home

function _shtyle_set { export PROMPT_ICON=${1:-'>:'} PROMPT_COLOR=${2:-'white'} PROMPT_ACCENT=${3:-'white'} PROMPT_BODY=${4:-'default'}; }
function shtyle { _shtyle_set $@; echo "$PROMPT_ICON" "$PROMPT_COLOR" "$PROMPT_ACCENT" "$PROMPT_BODY" > "$HOME/.shtyle"; }
if [[ ! -f "$HOME/.shtyle" ]]; then shtyle
else _shtyle_set `<"$HOME/.shtyle"`; fi


function pro { [[ "$workspace" ]] || local workspace="$PROJECTS"
  mkdir -p "$workspace" && cd "$workspace" || return 1
  if [[ "$1" == '-' ]]; then pro $__pro_prev; return
  else export __pro_prev=$__pro_repo; fi
  if [[ "$1" == 'clone' && "$2" ]]; then local repo="$2"; shift 2
    git clone "$repo" $@ || return 1; repo="${repo##*/}"; repo="${repo//.git/}"
    [[ -d "$repo" ]] && cd "$repo"
  else
    if [[ "$1" =~ ^[0-9a-zA-Z]+'/'[0-9a-zA-z]+$ ]]; then
      local depth=2 proj="$($awk -F/ '{print $1".*/.*"$2".*"}'<<<"$1")"
    else local depth=1 proj="$1"; fi
    local repo="`find . -maxdepth $depth -type d | grep "$proj" | head -1`"
    if [[ ! -d "$repo" ]]; then echo "$1 not found."; return 1; fi
    cd "$repo"; [[ "$2" ]] && shift && $@ .
  fi
  export __pro_repo=$repo
}

function cons { (mkdir -p "$CONS" && cd "$CONS" && edi . $(while [[ "$1" ]]; do printf " ${1//.md/}.md"; shift; done)); }

function holo { mkdir -p "$HOLO" || return 1
  if [[  "$1" == 'pro' ]]; then shift; workspace="$HOLO" pro "$@"; else edi "$HOLO"; fi
}

function adbdev {
  if [[ "$1" ]]; then local device="`adbb ls | grep -iE "$1" | head -1`"
    if [[ "$device" ]]; then export ANDROID_SERIAL="$device"
    else echo "no matching device: $1"; fi
  fi
  echo "ANDROID_SERIAL=$ANDROID_SERIAL"
}

function pyvenv { local env="${1:-.venv}"
  if [[ ! -d "$env" ]]; then $python -m venv "$env"; fi
  source "$env/bin/activate"
}

alias binsymbols='nm -gDC'
alias fastbroot='sudo "$ANDROID_HOME/platform-tools/fastboot"'
alias virtinit='pkg i virt-manager && sudo usermod -aG libvirt $USER'
alias rad='./gradlew'
alias mockitoinit='cd ./test && mkdir -p ./resources/mockito-extensions && echo mock-maker-inline > ./resources/mockito-extensions/org.mockito.plugins.MockMaker'
alias emu='adbb emu'
alias json='$python -m json.tool'
alias jsonprint='json | bat --language javascript'
alias cg='${botagent:-codex}'

alias rustupup="curl --proto '=https' --tlsv1.2 -sSf 'https://sh.rustup.rs' | sh;"
alias sdkmanup='curl -s "https://get.sdkman.io" | bash;'

alias nvmup='curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash;'
if [[ -d "$HOME/.nvm"  && -s "$HOME/.nvm/nvm.sh" && -s "$HOME/.nvm/bash_completion" ]]; then
  export NVM_DIR="$HOME/.nvm"; source "$NVM_DIR/nvm.sh" "$NVM_DIR/bash_completion"
  function nodepath { export NODE_PATH="`type npm &>/dev/null && npm root -g ||:`"; }
fi

alias pythonup='curl -fsSL https://pyenv.run | bash'
if [[ -d "$HOME/.pyenv" ]]; then
  export PYENV_ROOT="$HOME/.pyenv"
  include "$PYENV_ROOT/bin"
  eval "$(pyenv init - bash)"
  eval "$(pyenv virtualenv-init -)"
fi

alias flutterup='surely "install flutter ${flutter_version:-stable}" && cd && \
  rm -fr .pub-cache .flutter .flutter-devtools .dart .dartServer "$HOMELIB/flutter" && \
  cd "$HOMELIB" && git clone https://github.com/flutter/flutter.git && cd flutter && \
  git checkout "${flutter_version:-stable}" && bin/flutter doctor'
alias fpg='flutter pub get;'
alias fpup='flutter pub upgrade;'
alias ftest='flutter test;'

alias mul='clac mul' add='clac add' sub='clac sub' div='clac div' avg='clac avg'

if [[ "$whereami" == 'android' ]]; then
  export packages='build-essential python3 nodejs openjdk-21 which openssh screen bat rsync proot-distro whois'
  function proots { if [[ "$1" ]]; then proot-distro login "$@"; else proot-distro list; fi; }
elif [[ "$whereami" == 'darwin' ]]; then
  export packages='bash zsh coreutils gnu-sed watch bash-completion zsh-completion gpg qemu bat lolcat openssl screen brew-gem grep lazydocker'
  export packages_apps='visual-studio-code windows-app slack discord jetbrains-toolbox colima lima lima-additional-guestagents' # iterm2 utm qmk/qmk/qmk homebrew/cask-drivers/qmk-toolbox
  export sed='gsed'
  alias prefs='app "System Settings"' slk='app Slack' rdp='app "Microsoft Remote Desktop"' qmkt='app "QMK Toolbox"' screensave='app ScreenSaverEngine.app'
  alias fmy='open /System/Applications/FindMy.app' imsg='open /System/Applications/Messages.app'
  alias simulatorwipe='fastlane snapshot reset_simulators'
  alias xcodeinit='sudo xcode-select -s /Applications/Xcode.app/Contents/Developer'
  alias smc='if uname -m | grep -q arm64; then echo cpu_power,gpu_power,thermal; else echo smc; fi | if read samplers; then sudo powermetrics --samplers $samplers; fi;'
  alias rosetta_install='softwareupdate --install-rosetta'
  include /opt/homebrew/bin \
    /opt/homebrew/opt/coreutils/libexec/gnubin \
    /usr/local/opt/coreutils/libexec/gnubin \
    /opt/homebrew/opt/gnu-sed/libexec/gnubin \
    /opt/homebrew/opt/grep/libexec/gnubin \
    /Applications/Docker.app/Contents/Resources/bin
  function adduser { sudo sysadminctl -addUser $1 -password - -admin; }
elif [[ "$whereami" == *'bsd' ]]; then
  export packages='xorg xrdp xfce gnu-watch sudo coreutils date screen gsed hwstat drm-kmod rsync'
  export sed='gsed'
  alias lspci='sudo pciconf -l'
  alias lsusb='sudo usbconfig'
elif [[ "$whereami" == 'linux' ]]; then
  export packages='bash zsh build-essential gpg cmus iftop bat lolcat screen'
  export packages_apps='qemu qemu-kvm ioquake3-server steam-installer lm-sensors blender gimp xfce4 blackbox blackbox-themes xterm'
elif [[ "$whereami" == 'windows' ]]; then
  export packages='rsync gcc procps-ng diffutils mingw-w64-ucrt-x86_64-bat'
  export psh_history="$APPDATA/Microsoft/Windows/PowerShell/PSReadLine/ConsoleHost_history.txt"
  alias slk='secretly "$LOCALAPPDATA/slack/slack.exe"'
  alias winvars='rundll32 sysdm.cpl,EditEnvironmentVariables'
  alias hypervset='bcdedit -set hypervisorlaunchtype' # auto
  export PATH="/usr/bin:$PATH"
fi

function chhome { local HOME="`realpath "$@"`"; if [[ -d "$HOME" ]]; then cd "$HOME" && exec "$SHELL"; fi; }

function why {
  if [[ "$2" == 'fun' ]]; then
    why "$1" | grep '^function ' | $awk '{print $2}'; return
  elif [[ "$1" ]]; then local _command_="`command -v "$1" 2> /dev/null`"
  else return; fi
  if [[ "$_command_" == "$1" ]]; then echo -e "#!$SHELL\n## `type "$_command_"`" | $bat
  elif is binary "$_command_"; then echo "$1 is a binary: $_command_" | $bat
  elif is file "$_command_"; then echo -e "`$bat "$_command_"`\n\n##$_command_" | $bat
  elif ! is empty "$_command_"; then echo -e "#!$SHELL\n$_command_" | $bat
  else echo "$1 not found" | $bat; fi
}

if type bat &>/dev/null; then bat='bat'; else bat='cat'; fi; export bat

if [[ ! -f "$HOME/.vimrc" ]]; then cp "$DOTHOME/conf/vimrc" "$HOME/.vimrc"; fi

alias qq='exit'
alias iommus='for device in /sys/kernel/iommu_groups/*/devices/*; do group=${device#*/iommu_groups/*}; echo "group ${group%%/*} `lspci -nns ${device##*/}`"; done | gg "Group [[:digit:]]|Group [[:digit:]][[:digit:]]"'
alias stresstest='stress -c "${cores:-"4"}" -i "${io:-"1"}" -m "${memory:-"1"}" -t "${time:-"60"}"'
alias zon='web zon'

export awk="${awk:-gawk}" sed="${sed:-sed}" python="${python:-python3}"
if ! type "$awk" &>/dev/null; then awk='awk'; fi

export GREP_COLORS="ms=`ansi_esc=none ansicode bright,$PROMPT_COLOR`"
export BAT_STYLE='grid,numbers' BAT_PAGER=''
export GPG_TTY="`tty`"
export EDITOR='vim'

alias up='homeup'
alias home='edi "$DOTHOME";'
alias prolo='mkdir -p "$HOLO" && vim "$HOLO/profile" && source "$HOLO/profile"' profo='vim "$HOME/.profo" && source "$HOME/.profo"'
alias notes='holo; mkdir -p "$HOLO/doc"; edi "$HOLO/doc/notes.md"'
alias ssh-keygen-ed='ssh-keygen -t ed25519'
alias ssh-keygen-rsa='ssh-keygen -t rsa'
alias ssh-regen='ssh-keygen -y -f'
alias passgen='openssl passwd' # -1
alias fin='surely sure logout && kill -15 -1'
