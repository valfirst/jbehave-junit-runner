Scenario: Multiplication

Given a variable x with value <number>
When I multiply x by <factor>
Then x should equal <result>

Examples:
| number | factor | result |
|      1 |      1 |      1 |
|      0 |   1000 |      0 |
|    -10 |      0 |      0 |

Scenario: 2 x 3 success with given stories

GivenStories: 	org/jbehave/scenario/finegrained/junit/monitoring/Init.story,
				org/jbehave/scenario/finegrained/junit/monitoring/Greetings.story

Given a variable x with value 3
When I multiply x by 2
Then x should equal 6

Scenario: 2 x 3 fail with given stories

GivenStories: 	org/jbehave/scenario/finegrained/junit/monitoring/Init.story,
				org/jbehave/scenario/finegrained/junit/monitoring/Greetings.story

Given a variable x with value 3
When I multiply x by 2
Then x should equal 7

Scenario: 3 x 3 success 

Given a variable x with value 3
When I multiply x by 3
Then x should equal 9

Scenario: 3 x 3 fail

Given a variable x with value 3
When I multiply x by 3
Then x should equal 10
