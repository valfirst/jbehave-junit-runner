package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.everyItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.Steps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JUnitDescriptionGeneratorTest {

	private static final String DEFAULT_STORY_NAME = "Default Story Name";
	private static final String DEFAULT_SCENARIO_TITLE = "Default Scenario Title";
	@Mock
	StepCandidate stepCandidate;
	@Mock
	Steps steps;
	@Mock
	Story story;
	@Mock 
	Scenario scenario;

	private JUnitDescriptionGenerator generator;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(steps.listCandidates()).thenReturn(Arrays.asList(new StepCandidate[] {stepCandidate}));
		when(stepCandidate.matches(anyString())).thenReturn(true);
		when(stepCandidate.getStepsInstance()).thenReturn(new Object());
		when(story.getName()).thenReturn(DEFAULT_STORY_NAME);
		when(scenario.getTitle()).thenReturn(DEFAULT_SCENARIO_TITLE);
		generator = new JUnitDescriptionGenerator(Arrays.asList(new CandidateSteps[] {steps}));
	}

	@Test
	public void shouldGenerateDescriptionForTopLevelScenario() {
		when(scenario.getTitle()).thenReturn("MyTitle");
		Description description = generator.createDescriptionFrom(scenario);
		assertThat(description, equalTo(Description.createSuiteDescription("Scenario: MyTitle")));
	}

	@Test
	public void shouldGenerateDescriptionForStep() {
		when(scenario.getSteps()).thenReturn(Arrays.asList("Step1"));
		Description description = generator.createDescriptionFrom(scenario);
		assertThat(description.getChildren(), hasItem(step1Description()));
	}

	private Description step1Description() {
		return Description.createTestDescription(Object.class, "Step1");
	}

	@Test
	public void shouldGenerateDescriptionForStory() {
		Description description = generator.createDescriptionFrom(story);
		assertThat(description, is(Description
				.createSuiteDescription(DEFAULT_STORY_NAME)));
	}

	@Test
	public void shouldGenerateDescriptionForScenarioChildOfStory() {
		when(story.getScenarios()).thenReturn(Arrays.asList(new Scenario[] {scenario}));
		Description description = generator.createDescriptionFrom(story);
		assertThat(description.getChildren(), hasItem(Description.createSuiteDescription("Scenario: " + DEFAULT_SCENARIO_TITLE)));
	}

	@Test
	public void shouldCopeWithSeeminglyDuplicateSteps() throws Exception {
		when(scenario.getSteps()).thenReturn(Arrays.asList(new String[] {"Step1", "Step1"}));
		Description description = generator.createDescriptionFrom(scenario);
		assertThat(description.getChildren(), everyItem(Matchers.<Description>hasProperty("displayName", startsWith("Step1"))));
		assertThat(description.getChildren().size(), is(2));
		assertThat(description.getChildren(), allChildrenHaveUniqueDisplayNames());
	}

	private Matcher<List<Description>> allChildrenHaveUniqueDisplayNames() {
		return new BaseMatcher<List<Description>>() {

			private List<Description> descriptions;

			@SuppressWarnings("unchecked")
			public boolean matches(Object descriptions) {
				this.descriptions = (List<Description>) descriptions;
				Set<String> displayNames = new HashSet<String>();
				for (Description child : this.descriptions) {
					displayNames.add(child.getDisplayName());
				}
				return displayNames.size() == this.descriptions
						.size();
			}

			public void describeTo(org.hamcrest.Description description) {
				description
						.appendText("Children of description do not have unique display names");
				for (Description child : descriptions) {
					description.appendText(child.getDisplayName());
				}
			}

		};
	}
}