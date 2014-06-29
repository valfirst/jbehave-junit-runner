Scenario: 2 squared

Given a variable x with value 2
When I multiply x by 2 
Then x should equal 4

Scenario: 2x3

Given a variable x with value 3
When I multiply x by 2
Then x should equal 6

Scenario: 5 multiplied by 1

Given a variable x with value 5
When I multiply x by 1
Then x should equal 5 

Scenario: 6 multiplied by 2

Given a variable x with value 6
When I multiply x by 2
Then x should equal 12

Scenario: 2 multiplied with multiple values

Given a variable x with value 2

!-- This step demonstrates the use of tabular parameters
When I multiply x with all of:
| Value  |
| 3      |
| 4      |
| 5      |
Then x should equal 120

Scenario: Something with a pending step

Given a variable x with value 6
!-- The next step is pending and should prevent the rest from being
!-- executed at all
Given a pending step
When I multiply x by 2
Then x should equal 12
