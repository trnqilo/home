
# code

## follow coding best practices

+ SOLID design
  - single responsibility: keep classes and modules focused on doing one job well
  - open/closed: make classes and modules open for extension but closed for modification
  - liskov substitution: ensure derived types can replace base types seamlessly
  - interface segregation: prefer small, focused interfaces over large monolithic ones
  - dependency inversion: depend on abstractions rather than concrete implementations
+ DRY
  - consolidate duplicated logic into reusable functions or abstractions
  - balance reusability with readability to avoid over-abstraction
+ KISS and YAGNI
  - select the simplest solution that fulfills current requirements
  - avoid writing spec-driven code for features that are not currently needed
+ align code with the domain
  - use domain terminology in naming and model business rules explicitly inside core logic
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
