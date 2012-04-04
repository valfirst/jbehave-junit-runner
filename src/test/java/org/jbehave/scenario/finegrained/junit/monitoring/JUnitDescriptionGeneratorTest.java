package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.Steps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JUnitDescriptionGeneratorTest {

	@Mock
	StepCandidate StepCandidate;
	@Mock
	Steps steps;
//	@Mock
//	StoryDefinition story;

	private JUnitDescriptionGenerator generator;

	@Before
	public void setUp() {
//		MockitoAnnotations.initMocks(this);
//		stub(steps.listCandidates()).toReturn(new StepCandidate[] { StepCandidate });
//		stub(StepCandidate.matches((String) anyObject())).toReturn(true);
//		generator = new JUnitDescriptionGenerator();
	}

	@Test
	public void shouldGenerateDescriptionForTopLevelScenario() {
//		ScenarioDefinition scenario = new ScenarioDefinition("MyTitle");
//		Description description = generator.createDescriptionFrom(scenario,
//				steps);
//		ensureThat(description, equalTo(Description.createSuiteDescription("Scenario: MyTitle")));
	}

	@Test
	public void shouldGenerateDescriptionForStep() {
//		ScenarioDefinition scenario = new ScenarioDefinition("MyTitle", "Step1");
//		Description description = generator.createDescriptionFrom(scenario,
//				steps);
//		ensureThat(description.getChildren().size(), equalTo(1));
//		ensureThat(description.getChildren().get(0), equalTo(Description
//				.createTestDescription(steps.getClass(),
//						"Step1 - Scenario: MyTitle")));
	}

	@Test
	public void shouldGenerateDescriptionForStory() {
//		stub(story.getScenarios()).toReturn(
//				Collections.<ScenarioDefinition> emptyList());
//		Description description = generator.createDescriptionFrom(story, steps,
//				JUnitScenario.class);
//		ensureThat(description, equalTo(Description
//				.createSuiteDescription(JUnitScenario.class)));
	}

	@Test
	public void shouldGenerateDescriptionForScenarioChildOfStory() {
//		stub(story.getScenarios()).toReturn(
//				Arrays.asList(new ScenarioDefinition("MyTitle")));
//		Description description = generator.createDescriptionFrom(story, steps,
//				JUnitScenario.class);
//		ensureThat(description.getChildren().size(), equalTo(1));
//		ensureThat(description.getChildren().get(0), equalTo(Description
//				.createSuiteDescription("Scenario: MyTitle")));
	}

	@Test
	public void shouldCopeWithSeeminglyDuplicateSteps() throws Exception {
//		ScenarioDefinition scenario = new ScenarioDefinition("MyTitle",
//				"Step1", "Step2", "Step3", "Step2", "Step2");
//		Description description = generator.createDescriptionFrom(scenario,
//				steps);
//		ensureThat(description.getChildren().size(), equalTo(5));
//		ensureThat(description, allChildrenHaveUniqueDisplayNames());
	}

	private Matcher<Description> allChildrenHaveUniqueDisplayNames() {
		return new BaseMatcher<Description>() {

			private Description junitDescription;

			public boolean matches(Object item) {
				junitDescription = (Description) item;
				Set<String> displayNames = new HashSet<String>();
				for (Description child : junitDescription.getChildren()) {
					displayNames.add(child.getDisplayName());
				}
				return displayNames.size() == junitDescription.getChildren()
						.size();
			}

			public void describeTo(org.hamcrest.Description description) {
				description
						.appendText("Children of description do not have unique display names");
				for (Description child : junitDescription.getChildren()) {
					description.appendText(child.getDisplayName());
				}
			}

		};
	}
}