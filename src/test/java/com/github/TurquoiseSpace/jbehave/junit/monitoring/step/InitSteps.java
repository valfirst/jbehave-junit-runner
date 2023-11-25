package com.github.TurquoiseSpace.jbehave.junit.monitoring.step;

import java.sql.SQLException;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.ScenarioType;

public class InitSteps {
	@BeforeStories
	public void doSomethingBeforeStories() throws SQLException {
		System.out.println("InitSteps.doSomethingBeforeStories()");
		throw new SQLException("DU doof!");
	}

	@BeforeStory(uponGivenStory = true)
	public void doSomethingBeforeGivenStories() {
		System.out.println("InitSteps.doSomethingBeforeGivenStories()");
	}

	@BeforeStory(uponGivenStory = false)
	public void doSomethingBeforeRegularStories() {
		System.out.println("InitSteps.doSomethingBeforeRegularStories()");
	}

	@BeforeScenario(uponType = ScenarioType.NORMAL)
	public void doSomethingBeforeNormalScenario() {
		System.out.println("InitSteps.doSomethingBeforeNormalScenario()");
	}

	@BeforeScenario(uponType = ScenarioType.EXAMPLE)
	public void doSomethingBeforeExample() {
		System.out.println("InitSteps.doSomethingAfterAnyNormalScenario()");
	}

	@AfterScenario(uponType = ScenarioType.EXAMPLE, uponOutcome = Outcome.ANY)
	public void doSomethingAfterAnyExampleScenario() {
		System.out.println("InitSteps.doSomethingAfterAnyExampleScenario()");
	}

	@AfterScenario(uponType = ScenarioType.EXAMPLE, uponOutcome = Outcome.FAILURE)
	public void doSomethingAfterFailedExampleScenario() {
		System.out.println("InitSteps.doSomethingAfterFailedExampleScenario()");
	}

	@AfterScenario(uponType = ScenarioType.EXAMPLE, uponOutcome = Outcome.SUCCESS)
	public void doSomethingAfterSuccessfulExampleScenario() {
		System.out
				.println("InitSteps.doSomethingAfterSuccessfulExampleScenario()");
	}

	@AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.ANY)
	public void doSomethingAfterAnyNormalScenario() {
		System.out.println("InitSteps.doSomethingAfterAnyNormalScenario()");
	}

	@AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.FAILURE)
	public void doSomethingAfterFailedNormalScenario() {
		System.out.println("InitSteps.doSomethingAfterFailedNormalScenario()");
	}

	@AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.SUCCESS)
	public void doSomethingAfterSuccessfulNormalScenario() {
		System.out
				.println("InitSteps.doSomethingAfterSuccessfulNormalScenario()");
	}

	@AfterStory(uponGivenStory = false)
	public void doSomethingAfterRegularStories() {
		System.out.println("InitSteps.doSomethingAfterRegularStories()");
	}

	@AfterStory(uponGivenStory = true)
	public void doSomethingAfterGivenStories() {
		System.out.println("InitSteps.doSomethingAfterGivenStories()");
	}

	@AfterStories
	public void doSomethingAfterStories() {
		System.out.println("InitSteps.doSomethingAfterStories()");
	}
}
