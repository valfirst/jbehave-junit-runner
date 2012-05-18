package de.codecentric.jbehave.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class JUnitReportingRunnerTest {

	@Mock
	private RunNotifier notifier;
	private JUnitReportingRunner runner;
	private String expectedDisplayName;
	private String expectedFirstStoryName;
	private Description description;

	@Before
	public void setUp() throws Throwable {
		MockitoAnnotations.initMocks(this);
		description = runner.getDescription();
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] params = {
				{ ExampleScenarioJUnitStories.class,
						ExampleScenarioJUnitStories.class.getName(),
						"Multiplication.story" },
				{ ExampleScenarioJUnitStory.class,
						ExampleScenarioJUnitStory.class.getName(),
						"example_scenario_j_unit_story.story" } };
		return Arrays.asList(params);
	}

	public JUnitReportingRunnerTest(Class<? extends ConfigurableEmbedder> cls,
			String expectedDisplayName, String expectedFirstStoryName)
			throws Throwable {
		runner = new JUnitReportingRunner(cls);
		this.expectedDisplayName = expectedDisplayName;
		this.expectedFirstStoryName = expectedFirstStoryName;
	}

	@Test
	public void runUpExampleScenarioAndCheckNotifications() {
		runner.run(notifier);
		verifyAllChildDescriptionsFired(description, true);
	}

	private void verifyAllChildDescriptionsFired(Description description,
			boolean onlyChildren) {
		String displayName = description.getDisplayName();
		if (!onlyChildren && considerStepForVerification(description)) {
			verify(notifier).fireTestStarted(description);
			System.out.println("verified start " + displayName);
		}
		for (Description child : description.getChildren()) {
			verifyAllChildDescriptionsFired(child, false);
		}
		if (!onlyChildren && considerStepForVerification(description)) {
			verify(notifier).fireTestFinished(description);
			System.out.println("verified finish " + displayName);
		}
	}

	private boolean considerStepForVerification(Description d) {
		String displayName = d.getDisplayName();
		return Character.isDigit(displayName.charAt(displayName.length() - 1));
	}

	@Test
	public void topLevelDescriptionForExample() {
		assertThat(description.getDisplayName(), equalTo(expectedDisplayName));
	}

	@Test
	public void storyDescriptionsForExample() {
		assertThat(getFirstStory().getDisplayName(),
				equalTo(expectedFirstStoryName));
	}

	@Test
	public void scenarioDescriptionsForExample() {
		assertThat(getFirstScenario().getDisplayName(),
				equalTo("Scenario: 2 squared"));
	}

	@Test
	public void stepDescriptionsForExample() {
		assertThat(getFirstScenario().getChildren().get(0).getDisplayName(),
				Matchers.startsWith("Given a variable x with value 2"));
	}

	private Description getFirstStory() {
		return description.getChildren().get(1);
	}

	private Description getFirstScenario() {
		return getFirstStory().getChildren().get(0);
	}

}
