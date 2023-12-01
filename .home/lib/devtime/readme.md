# devtime

+ install packages `bash gawk git grep tmux vim watch coreutils gnu-sed`
+ setup
  + add `devtime` script to PATH
  + optional: change devtime dir `export DEVTIME="$HOME/devtimes"`
  + optional: init repo `cd && git clone my_repo_url .devtime`
+ run `devtime`
+ start a new task `> + new task text` or `> ... another task`
+ start from time `> + 01:23 task` or `> ... 12:34 task`
+ enter a note `> note text` or `> - note text`
  - enter an indented note `> -- two tabs over`
  * add a quest note `> ? something needs solving`
+ take a break `> afk` or `> afk 23:45`
+ view hours for all days `> forever`
+ edit current log with vim `> -`
+ open workspace in vscode `> code`
+ remove last line `> pop`
+ tag words using `> #these @will +be !highlighted`
  + create a tag `> tag epic`
  + edit tags file `> tag`
+ finish the day `> gg`
+ end session `> q`

+ optionally
