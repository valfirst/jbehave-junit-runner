package org.jbehave.scenario.finegrained.junit.monitoring.step;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Assert;

public class ExampleSteps {
    int x;
    
    @Given("a variable x with value $value")
    public void givenXValue(@Named("value") int value) {
        x = value;
    }
    
    @When("I multiply x by $value")
    public void whenImultiplyXBy(@Named("value") int value) {
        x = x * value;
    }
    
    @Then("x should equal $value")
    public void thenXshouldBe(@Named("value") int value) {
        Assert.assertEquals(value, x);
    }
}
