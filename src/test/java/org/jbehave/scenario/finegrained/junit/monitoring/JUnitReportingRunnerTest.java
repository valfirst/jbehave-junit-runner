package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.verify;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
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
	public void setUp() {
		MockitoAnnotations.initMocks(this);
//		runner = new JUnitReportingRunner(
//				ExampleScenario.class);
//		description = runner.getDescription();
	}

	@Test
	public void runUpExampleScenarioAndCheckNotifications() {
//		runner.run(notifier);
//		verifyAllChildDescriptionsFired(description);
	}

	private void verifyAllChildDescriptionsFired(Description description) {
		verify(notifier).fireTestStarted(description);
		verify(notifier).fireTestFinished(description);
		for (Description child : description.getChildren()) {
			verifyAllChildDescriptionsFired(child);
		}
	}
	
	@Test
	public void topLevelDescriptionForExample() {
//		ensureThat(description.getDisplayName(), equalTo("org.jbehave.scenario.finegrained.junit.monitoring.ExampleScenario"));
	}
	
	@Test
	public void scenarioDescriptionsForExample() {
//		ensureThat(description.getChildren().get(0).getDisplayName(), equalTo("Scenario: 2 squared"));
	}
	
	@Test
	public void stepDescriptionsForExample() {
//		ensureThat(description.getChildren().get(0).getChildren().get(0).getDisplayName(), startsWith("Given a variable x with value 2"));
	}
	
	private Matcher<String> startsWith(final String prefix) {
		return new BaseMatcher<String>() {

			public boolean matches(Object item) {
				return item.toString().indexOf(prefix) == 0;
			}

			public void describeTo(org.hamcrest.Description description) {
				description.appendText("A String starting with \"" + prefix + "\"");
			}
			
		};
		
	}

}
