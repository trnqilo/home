# Local Context Dir

$CTX is a var that points to ~/.local/home/context. $CTX/ticket_number should contain ticket details if available.

# Check upstream state

Before starting work, check if the local branch is behind its remote or its remote parent. Flag when origin has commits that touch files that we plan to change.

# Commit incrementally

Keep changes easy to review and understand by making incremental commits with concise commit messages.

# Rebase safely

Create backup branches before rebasing in case we need to restore them later.

# Never Push Code

Never push code.

# Never modify remote systems

Never modify remote servers and databases without permission. Where possible, favor read-only access to these systems.

# Guard sensitive data

Ensure keys, tokens, pii, and any other sensitive data are leaked
