package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class JUnitReportingRunnerTest {

	@Mock
	private RunNotifier notifier;
	private JUnitReportingRunner runner;
	private Description description;

	@Before
	public void setUp() throws Throwable {
		MockitoAnnotations.initMocks(this);
		runner = new JUnitReportingRunner(
				ExampleScenario.class);
		description = runner.getDescription();
	}

	@Test
	public void runUpExampleScenarioAndCheckNotifications() {
		runner.run(notifier);
		verifyAllChildDescriptionsFired(description, true);
	}

	private void verifyAllChildDescriptionsFired(Description description, boolean onlyChildren) {
		if (!onlyChildren) {
			verify(notifier).fireTestStarted(description);
			System.out.println("verified start "+description.getDisplayName());
		}
		for (Description child : description.getChildren()) {
			verifyAllChildDescriptionsFired(child, false);
		}
		if (!onlyChildren) {
			verify(notifier).fireTestFinished(description);
			System.out.println("verified finish "+description.getDisplayName());
		}
	}
	
	@Test
	public void topLevelDescriptionForExample() {
		assertThat(description.getDisplayName(), equalTo("org.jbehave.scenario.finegrained.junit.monitoring.ExampleScenario"));
	}
	
	@Test
	public void storyDescriptionsForExample() {
		assertThat(getFirstStory().getDisplayName(), equalTo("Multiplication.story"));
	}

	@Test
	public void scenarioDescriptionsForExample() {
		assertThat(getFirstScenario().getDisplayName(), equalTo("Scenario: 2 squared"));
	}

	@Test
	public void stepDescriptionsForExample() {
		assertThat(getFirstScenario().getChildren().get(0).getDisplayName(), Matchers.startsWith("Given a variable x with value 2"));
	}
	
	private Description getFirstStory() {
		return description.getChildren().get(1);
	}

	private Description getFirstScenario() {
		return getFirstStory().getChildren().get(0);
	}

}

