package com.github.TurquoiseSpace.jbehave.junit.monitoring.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;

public class ExampleSteps {
	private int x;
	private Map<String, Integer> variables;
	private int result;

	@Given("a variable x with value $value")
	@Alias("ist die Variable x mit dem Wert $value")
	public void givenXValue(@Named("value") int value) {
		x = value;
	}

	@When("I multiply x by $value")
	@Alias("ich x mit $value multipliziere")
	public void whenImultiplyXBy(@Named("value") int value) {
		x = x * value;
	}

	@When("I multiply x with all of:$param")
	@Alias("ich x mit folgenden Werten multipliziere:$param")
	public void whenImultiplyXByOneOf(ExamplesTable param) {
		for (Parameters p : param.getRowsAsParameters()) {
			Integer value = p.valueAs("Value", Integer.class);
			x = x * value;
		}
	}

	@Then("x should equal $value")
	@Alias("ist x gleich $value")
	public void thenXshouldBe(@Named("value") int value) {
		assertEquals(value, x);
	}

	@Given("some initialization")
	public void givenSomeInitialization() {
		System.out.println("Init");
	}

	@Given("a Greeting to $somebody")
	public void givenAGreetingToSomebody(@Named("somebody") String somebody) {
		System.out.println("Hello " + somebody);
	}

	@Given("the variables: $variables")
	public void givenTheVariables(ExamplesTable table) {
		variables = new HashMap<>();
		for (Map<String, String> row : table.getRows()) {
			variables.put(row.get("name"), Integer.valueOf(row.get("value")));
		}
	}

	@When("all variables are multiplied")
	public void allVariablesAreMultipled() {
		result = 1;
		for (Integer variable : variables.values()) {
			result *= variable;
		}
	}

	@Then("the result should be $result")
	public void theResultShouldBe(@Named("result") String result) {
		assertEquals(result, "" + this.result);
	}

	@Given("a complex situation")
	@Composite(steps = { "Given a variable x with value 1",
			"When I multiply x by 5" })
	public void aComplexSituation() {
		// This is complex case with single method representing Composite step and Given step at the same time
	}

	@When("this step fails")
	public void thisStepFails() {
		fail("this step failed on purpose");
	}

	@Then("this step is not executed")
	public void thisStepIsNotExecuted() {
		// This step is to help document a scenario where a prior step is expected to fail and this step will not execute.
	}
}
