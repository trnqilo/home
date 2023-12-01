if [[ "$SHELL" != *'zsh' || ! "$DOTHOME" ]]; then
source "$HOME/.home/profile"

export HISTFILE="$HOME/.zsh_history"
export HISTSIZE=10000
export SAVEHIST=10000

setopt interactive_comments
setopt noautomenu
setopt nomenucomplete
setopt PROMPT_SUBST
setopt SHARE_HISTORY

if type brew &>/dev/null; then
  export FPATH="`brew --prefix`/share/zsh-completions:$FPATH"
fi
autoload -Uz compinit
compinit -u
autoload -Uz vcs_info
function precmd { vcs_info; }
zstyle ':vcs_info:git:*' formats '(%b)'

bindkey "^[[1;5C" forward-word
bindkey "^[[1;5D" backward-word
bindkey "^[[H" beginning-of-line
bindkey "^[[F" end-of-line
bindkey "^[[3~" delete-char
bindkey "^U" backward-kill-line
bindkey -e
fi
PROMPT=
if [[ "$PROMPT_TAG" ]]; then PROMPT+="%F{\$PROMPT_COLOR}$PROMPT_TAG "; fi
PROMPT+='%F{'$PROMPT_COLOR'}'${PROMPT_USER:-%n}' %F{'$PROMPT_ACCENT'}'${PROMPT_HOST:-%m}' '${PROMPT_SHELL:-zsh}' %* %f%F{'$PROMPT_COLOR'}%~ %F{'$PROMPT_ACCENT'}${vcs_info_msg_0_}
%F{'$PROMPT_COLOR'}'$PROMPT_ICON' %f%F{'$PROMPT_BODY'}'
export PROMPT
