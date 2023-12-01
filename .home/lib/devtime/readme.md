# devtime

## setup
+ install `bash gawk git grep tmux vim watch coreutils gnu-sed` and run `./devtime`
+ or run with Docker `./devtime dock`
+ optional setup
  + add `devtime` script to PATH `export PATH="$PATH:/path/to/devtime"`
  + init repo `cd && git clone my_repo_url .devtime`
  + change devtime dir `export devtime_home="/path/to/.devtime"`
+ start session `devtime`
  - start session in devtime sub dir `devtime go proj123`

## run
+ start a new task `> + label` or `> + 12:34 label`
+ enter a note `> - note text` or `> note text`
  - indent note `> --- triple indent note text`
+ add a reminder with !! `> todo: solve me later !!`
+ take a break `> + afk` or `> + 23:45 afk`
+ use vim to open log `> #` or workspace `> ##`
+ remove last line `> pop`, or n lines `> pop pop pop`
+ highlight words using `> #these @will +be !highlighted`
+ finish the day `> gg` or `> gg 23:45`
+ leave session `> qq` or `ctrl+c`

## commands
+ tag words `> /tag epic story spike`, edit tags `> /tag`
+ open workspace in vscode `> /code`
+ validate all logs `> /validate`
+ run shell commands with care `> /dd if=/dev/zero of=/dev/*`
+ print hours by task for all days `> /tasks`
+ print hours by task for current week `> /week`
+ print total hours for all weeks `> /weeks`
+ print total hours for all days `> /forever`
+ print all logs and stats `> /report`

## questions
+ should devtime be used in production?
  + no
+ are the calculations in devtime accurate?
  + probably, use the validator
+ is the validator accurate?
  + probably
+ does devtime work past midnight?
  + untested. there may be consequences regardless
