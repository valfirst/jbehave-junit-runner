Meta:
@all

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: First scenario filtered by tag first
Meta: @first
Given a variable x with value 2
When I multiply x by 2
Then x should equal 4

Scenario: Second scenario filtered by tag second
Meta: @second
Then this step is not executed