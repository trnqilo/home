
# context

## use local context dir

+ $ctx is a var that points to a context docs dir
  - if unset, look for ~/.local/home/context or ~/Projects/home/context
+ any references to ctx/ should use this location
+ ctx/{{ticket_number}} should contain story/ticket details if available
  - reviews, pr summaries, and other generated docs should go into ctx/{{ticket_number}}
+ if ctx is in a git repo, commit when adding or changing files
