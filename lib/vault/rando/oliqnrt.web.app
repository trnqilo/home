#!/usr/bin/env bash
set -e
if type sudo &>/dev/null; then sudo=sudo; else sudo=; fi
if test "${OSTYPE#darwin}" != "$OSTYPE" && ! type brew &>/dev/null; then bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"; fi
if type git &>/dev/null; then :
elif type brew &>/dev/null; then brew update && brew install git
elif type apt &>/dev/null; then $sudo apt update && $sudo apt install -y git
elif type pkg &>/dev/null; then $sudo pkg update && $sudo pkg install -y git
elif type dnf &>/dev/null; then $sudo dnf update && $sudo dnf install -y git
elif type pkgin &>/dev/null; then $sudo pkgin update && $sudo pkgin install -y git
elif type apk &>/dev/null; then $sudo apk update -y && $sudo apk add --no-pkg-conflicts -y git
elif type pacman &>/dev/null; then $sudo pacman -Ss && $sudo pacman -S --needed --noconfirm git
elif type pkg_add &>/dev/null; then $sudo pkg_add -I git; fi
if type git &>/dev/null; then curl "https://raw.githubusercontent.com/trnqilo/home/refs/heads/${branch:-home}/readme" | bash
else echo 'git please...'; fi
exit
