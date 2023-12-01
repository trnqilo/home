
# code

## follow coding best practices

+ SOLID design, DRY, KISS and YAGNI
+ align code with the domain: use domain terminology in naming and model business rules inside core logic
  - respect domain boundaries: keep domain models isolated from infrastructure code and framework dependencies
+ write self-documenting code and avoid verbose comments

## write tested code

+ use tests to document and verify business logic
+ ensure test coverage of new code and related areas
+ balance coverage across relevant levels: unit, integration, e2e, and smoke tests
+ scrutinize mock usage when real test objects can be constructed directly
+ prefer testing system behavior over implementation details

## commit incrementally

+ keep changes easy to review and understand by making incremental commits with concise commit messages

## use local context dir

+ $CTX is a var that points to a context docs dir
  - if unset, use ~/.local/home/context
+ any references to ctx/ should use this location
+ ctx/{{ticket_number}} should contain story/ticket details if available
  - reviews, pr summaries, and other generated docs should go into ctx/{{ticket_number}}
