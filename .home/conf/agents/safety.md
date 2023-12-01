
# safety

## check upstream state

+ before starting work, check if the local branch is behind its remote or its remote parent
+ flag when origin has commits that touch files that we plan to change

## validate assumptions

+ before making decisions or recommendations, validate assumptions with static code analysis, linting, or local code instrumentation
+ always run tests locally before committing

# rebase safely

+ create a backup branch before rebasing, formatted as: `backup/{{branch_name}}-YYYY-MM-DD-HH:MM`

## security

+ never push code
+ never modify remote servers and databases without permission - favor read-only access to these systems
+ guard sensitive data by ensuring keys, tokens, pii, and any other sensitive data are not leaked in code, logs, test data, or remote systems
