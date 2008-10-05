package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.util.JUnit4Ensure.ensureThat;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;

public class ExampleSteps extends Steps {
    Integer x;
    
    @Given("a variable x with value $value")
    public void aVariableXWithValue(int value) {
	x=value;
    }
    
    @When("I multiply x by $value")
    public void iMultiplyXBy(int value) {
	x = value * x;
    }
    
    @Then("x should equal $value")
    public void xShouldEqual(int value) {
	ensureThat(x, equalTo(value));
    }

}
