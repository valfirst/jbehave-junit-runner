Scenario: Multiplication with tabular parameters

Given the variables:
| name | value |
|    x |     1 |
|    y |     2 |
|    z |     3 |
When all variables are multiplied
Then the result should be 6

Scenario: Something with a composite Step

Given a complex situation
When I multiply x by 3
Then x should equal 15

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

GivenStories: 	com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Init.story,
				com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Greetings.story,
				com/github/TurquoiseSpace/jbehave/junit/monitoring/story/GreetingsFromExtraterrestrials.story

Given a variable x with value 3
When I multiply x by 2
Then x should equal 6

Scenario: 2 x 3 fail with given stories

GivenStories: 	com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Init.story,
				com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Greetings.story

Given a variable x with value 3
When I multiply x by 2
Then x should equal 7

Scenario: parameterized given stories

GivenStories: 	com/github/TurquoiseSpace/jbehave/junit/monitoring/story/ParameterizedGreetings.story#{0},
				com/github/TurquoiseSpace/jbehave/junit/monitoring/story/ParameterizedGreetings.story#{1}

Given a Greeting to Parameterized Stories
And a Greeting to <name>
And a variable x with value 3
When I multiply x by 2
Then x should equal 7

Examples:

| name    |
| Daniel  |
| Andreas |

Scenario: non-parameterized given stories with example tables

GivenStories: 	com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Greetings.story

Given a Greeting to Parameterized Stories
And a Greeting to <name>
And a variable x with value 3
When I multiply x by 2
Then x should equal 7

Examples:

| name    |
| Daniel  |
| Andreas |

Scenario: 3 x 3 success

Given a variable x with value 3
When I multiply x by 3
Then x should equal 9

Scenario: 3 x 3 fail

Given a variable x with value 3
When I multiply x by 3
Then x should equal 10

Scenario: 10 x 100 with comments

!-- a comment
Given a variable x with value 10
!-- and another comment
When I multiply x by 100
!-- wow, that's a large number!
Then x should equal 1000
!-- got to do the calculations in jbehave...

Scenario: Some calculations with And steps -- which are finally failing.

Given a variable x with value 10
And a variable x with value -5
When I multiply x by 6
And I multiply x by 7
Then x should equal -210
And x should equal 210

Scenario: All but the first Steps are pending

Given a variable x with value 10
Given a pending step
And a pending step
When there is another pending step
And another pending step
Then this results in two pending steps
And two additional pending steps
