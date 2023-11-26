package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.story.ExampleScenarioJUnitStories;
import com.github.TurquoiseSpace.jbehave.junit.monitoring.story.ExampleScenarioJUnitStoriesLocalized;
import com.github.TurquoiseSpace.jbehave.junit.monitoring.story.ExampleScenarioJUnitStory;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(Parameterized.class)
public class JUnitReportingRunnerIntegrationTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private RunNotifier notifier;
	private JUnitReportingRunner runner;
	private String expectedDisplayName;
	private String expectedFirstStoryName;
	private String expectedFirstScenario;
	private String expextedFirstStep;

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] params = {
				{ ExampleScenarioJUnitStories.class,
						"Multiplication\u2024story",
						"Scenario: 2 squared",
						"Given a variable x with value 2" },
				{ ExampleScenarioJUnitStory.class,
						"example_scenario_j_unit_story\u2024story",
						"Scenario: 2 squared",
						"Given a variable x with value 2" },
				{ ExampleScenarioJUnitStoriesLocalized.class,
						"Multiplication_de\u2024story",
						"Szenario: 2 Quadrat",
						"Gegeben ist die Variable x mit dem Wert 2" } };
		return Arrays.asList(params);
	}

	public JUnitReportingRunnerIntegrationTest(
			Class<? extends ConfigurableEmbedder> cls,
			String expectedFirstStoryName, String expectedFirstScenario,
			String expextedFirstStep)
			throws Throwable {
		runner = new JUnitReportingRunner(cls);
		this.expectedDisplayName = cls.getName();
		this.expectedFirstStoryName = expectedFirstStoryName;
		this.expectedFirstScenario = expectedFirstScenario;
		this.expextedFirstStep = expextedFirstStep;
	}

	@Test
	public void runUpExampleScenarioAndCheckNotifications() {
		runner.run(notifier);
		verifyAllChildDescriptionsFired(runner.getDescription(), true);
	}

	private void verifyAllChildDescriptionsFired(Description description,
			boolean onlyChildren) {
		String displayName = description.getDisplayName();
		if (!onlyChildren && considerStepForVerification(description)) {
			verify(notifier).fireTestStarted(description);
			System.out.println("verified start " + displayName);
		}
		for (Description child : description.getChildren()) {
			verifyAllChildDescriptionsFired(child, true);
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
		assertThat(runner.getDescription().getDisplayName(),
				equalTo(expectedDisplayName));
	}

	@Test
	public void storyDescriptionsForExample() {
		assertThat(getFirstStory().getDisplayName(),
				equalTo(expectedFirstStoryName));
	}

	@Test
	public void scenarioDescriptionsForExample() {
		assertThat(getFirstScenario().getDisplayName(),
				equalTo(expectedFirstScenario));
	}

	@Test
	public void stepDescriptionsForExample() {
		assertThat(getFirstScenario().getChildren().get(0).getDisplayName(),
				Matchers.startsWith(expextedFirstStep));
	}

	private Description getFirstStory() {
		return runner.getDescription().getChildren().get(1);
	}

	private Description getFirstScenario() {
		return getFirstStory().getChildren().get(0);
	}

}
