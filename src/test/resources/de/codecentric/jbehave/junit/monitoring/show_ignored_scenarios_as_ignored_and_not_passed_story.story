Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: Only interested that each step passes so that scenario shows as green in IDE
Given a variable x with value 2
When I multiply x by 2
Then x should equal 4

Scenario: This scenario is ignored and not executed because it is filtered out using a meta tag
Meta: @skip
Then this step is not executed

