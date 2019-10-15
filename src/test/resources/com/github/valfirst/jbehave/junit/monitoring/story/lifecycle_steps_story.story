Lifecycle:
Before:
Scope: STORY
Given a variable x with value 2
Scope: SCENARIO
When I multiply x by 3
Scope: STEP
When I multiply x by 4
After:
Scope: STEP
When I multiply x by 6
Scope: SCENARIO
Outcome: ANY
When I multiply x by 7
Outcome: SUCCESS
When I multiply x by 8
Scope: STORY
Outcome: ANY
When I multiply x by 9
Outcome: SUCCESS
Then x should equal 362880

Scenario: x multiplied by 5
When I multiply x by 5
