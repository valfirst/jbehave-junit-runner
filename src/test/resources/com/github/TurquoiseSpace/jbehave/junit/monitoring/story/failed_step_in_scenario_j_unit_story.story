Scenario: Middle step fails and AfterStories shows as passed
          because there isn't a method annotated with AfterStories
          hence AfterStories can only be treated as passed

Given a variable x with value 2
When this step fails
Then this step is not executed