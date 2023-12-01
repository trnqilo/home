
# review

+ branches and prs
  - tickets may contain branch and pr information
    - if missing, attempt to find associated branches in the repo
  - if we cannot determine the relevant code for review, exit
  - include shas, branches and prs used in the review

+ review categories
  - code quality considerations such as performance/scale impact, structure, deduplication, and readability
  - test coverage, test code quality, and appropriate test levels
  - architecture, infrastructure, and security considerations

+ print the review and write it to ctx/{{ticket_number}}/{{ticket_number}}-review.md
