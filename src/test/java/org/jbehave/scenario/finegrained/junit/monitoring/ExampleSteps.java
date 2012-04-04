package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class ExampleSteps  {
	Integer x;

	@Given("a variable x with value $value")
	public void aVariableXWithValue(int value) {
		x = value;
	}

	@When("I multiply x by $value")
	public void iMultiplyXBy(int value) {
		x = value * x;
	}

	@Then("x should equal $value")
	public void xShouldEqual(int value) {
		assertThat(x, equalTo(value));
	}

}
