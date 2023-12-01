source "$HOME/.home/profile"
BASH_PROMPT_COLOR=`ansicode $PROMPT_COLOR`
BASH_PROMPT_ACCENT=`ansicode $PROMPT_ACCENT`
BASH_PROMPT_BODY=`ansicode $PROMPT_BODY`
export HISTSIZE=-1 HISTFILESIZE=-1
shopt -s histappend
shopt -s cmdhist
[[ -r "/usr/local/etc/profile.d/bash_completion.sh" ]] && . "/usr/local/etc/profile.d/bash_completion.sh"
if type __git_ps1 &> /dev/null; then GITPS1CMD='$(__git_ps1 "\[\e[$BASH_PROMPT_ACCENT\] (%s)")'; fi
export PS1="\[\e[$BASH_PROMPT_COLOR\]${PROMPT_USER:-\u}\[\e[m\] \[\e[$BASH_PROMPT_ACCENT\]${PROMPT_HOST:-\h}\[\e[m\] \[\e[$BASH_PROMPT_ACCENT\]${PROMPT_SHELL:-bash}\[\e[m\] \[\e[$BASH_PROMPT_ACCENT\]\t\[\e[$BASH_PROMPT_COLOR\] \w$GITPS1CMD
\[\e[$BASH_PROMPT_COLOR\]$PROMPT_ICON \[\e[m\]\[\e[$BASH_PROMPT_BODY\]"
if [[ "$PROMPT_TAG" ]]; then export PS1="\[\e[$BASH_PROMPT_COLOR\]$PROMPT_TAG\[\e[m\] $PS1"; fi
set enable-bracketed-paste on

alias dosh='PROMPT_COMMAND='"'"'PS1="\[\e[35m\]C:${PWD//\//\\\\}>'"$GITPS1CMD"'\[\e[0m\] "'"'"
alias rez='PROMPT_COMMAND='"'"'PS1="'$PS1'"'"'"
alias hide='PROMPT_COMMAND="PS1="'
