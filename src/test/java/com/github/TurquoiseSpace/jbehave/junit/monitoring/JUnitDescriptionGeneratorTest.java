package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.ExamplePerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.Steps;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class JUnitDescriptionGeneratorTest {

	private static final String DEFAULT_SCENARIO_TITLE = "Default Scenario Title";
	private static final String GIVEN_STEP = "Given Step1";

	private static final Class<?> STEPS_TYPE = Object.class;

	@Mock
	private StepCandidate stepCandidate;
	@Mock
	private CandidateSteps steps;
	@Mock
	private GivenStories givenStories;
	@Mock
	private Configuration configuration;

	private JUnitDescriptionGenerator generator;

	@Before
	public void setUp() {
		when(steps.listCandidates()).thenReturn(singletonList(stepCandidate));
		when(stepCandidate.matches(anyString(), ArgumentMatchers.isNull())).thenReturn(true);
		when(stepCandidate.getStepsType()).then((Answer<Class<?>>) invocation -> STEPS_TYPE);
		when(givenStories.getPaths()).thenReturn(Collections.emptyList());
		when(configuration.keywords()).thenReturn(new Keywords());
	}

	@Test
	public void shouldCountSteps() {
		Scenario scenario = createScenario(GIVEN_STEP);
		Story story = createStory(scenario);
		createDescriptionFrom(story);
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldNotCountComments() {
		Scenario scenario = createScenario(GIVEN_STEP, "!-- This is a comment");
		Story story = createStory(scenario);
		createDescriptionFrom(story);
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldCountIgnoredSteps() {
		Scenario scenario = createScenario(GIVEN_STEP, "!-- Then ignored step");
		Story story = createStory(scenario);
		createDescriptionFrom(story);
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldExcludeFilteredOutStory() {
		Story story = createStory(createScenario(GIVEN_STEP));
		createDescriptionFrom(false, true, story);
		assertEquals(0, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForStory() {
		String step1 = GIVEN_STEP;
		String step2 = "And step with table param:\n|Head|\n|Value|";

		when(stepCandidate.matches(step2, "Given ")).thenReturn(true);
		when(stepCandidate.getStartingWord()).thenReturn("Given");

		Lifecycle lifecycle = new Lifecycle(createBeforeLifecycleSteps(), createAfterLifecycleSteps());
		Scenario scenario = createScenario(step1, step2);
		Story story = createStory(lifecycle, scenario);
		List<Description> storyDescriptions = createDescriptionFrom(story);
		assertEquals(10, generator.getTestCases());
		assertEquals(1, storyDescriptions.size());
		Description storyDescription = storyDescriptions.get(0);
		assertEquals(Description.createSuiteDescription(story.getName()), storyDescription);
		List<Description> storyLevelDescriptions = asList(
				Description.createTestDescription(STEPS_TYPE, "Then before STORY"),
				Description.createSuiteDescription("Scenario: " + scenario.getTitle()),
				Description.createTestDescription(STEPS_TYPE, "Then after ANY STORY"));
		assertEquals(storyLevelDescriptions, storyDescription.getChildren());
		Description scenarioDescription = storyDescription.getChildren().get(1);
		List<Description> scenarioLevelDescriptions = asList(
				Description.createTestDescription(STEPS_TYPE, "Then before SCENARIO"),
				Description.createTestDescription(STEPS_TYPE, "Then before STEP"),
				Description.createTestDescription(STEPS_TYPE, step1),
				Description.createTestDescription(STEPS_TYPE, "Then after STEP"),
				Description.createTestDescription(STEPS_TYPE, "Then before STEP\u200B"),
				Description.createTestDescription(Object.class, "And step with table param:"),
				Description.createTestDescription(STEPS_TYPE, "Then after STEP\u200B"),
				Description.createTestDescription(STEPS_TYPE, "Then after ANY SCENARIO"));
		assertEquals(scenarioLevelDescriptions, scenarioDescription.getChildren());
	}

	@Test
	public void shouldStripLinebreaksFromScenarioDescriptions() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getGivenStories()).thenReturn(givenStories);
		when(scenario.getTitle()).thenReturn("Scenario with\nNewline");
		Story story = createStory(scenario);
		List<Description> descriptions = createDescriptionFrom(story);
		assertEquals(1, descriptions.size());
		assertThat(firstChild(descriptions.get(0)).getDisplayName(), not(containsString("\n")));
	}

	@Test
	public void shouldStripCarriageReturnsFromScenarioDescriptions() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getGivenStories()).thenReturn(givenStories);
		when(scenario.getTitle()).thenReturn("Scenario with\rCarriage Return");
		Story story = createStory(scenario);
		List<Description> descriptions = createDescriptionFrom(story);
		assertEquals(1, descriptions.size());
		assertThat(firstChild(descriptions.get(0)).getDisplayName(), not(containsString("\r")));
	}

	@Test
	public void shouldCopeWithSeeminglyDuplicateSteps() {
		Scenario scenario = createScenario(GIVEN_STEP, GIVEN_STEP);
		when(stepCandidate.matches(GIVEN_STEP, "Given ")).thenReturn(true);
		when(stepCandidate.getStartingWord()).thenReturn("Given");
		Description description = createDescriptionFrom(scenario);
		assertThat(description.getChildren(), everyItem(whoseDisplayName(startsWith(GIVEN_STEP))));
		assertEquals(2, description.getChildren().size());
		assertThat(description.getChildren(), allChildrenHaveUniqueDisplayNames());
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCopeWithDuplicateGivenStories() {
		Story story = createStory(createScenario(), createScenario());
		when(givenStories.getPaths()).thenReturn(singletonList("/some/path/to/GivenStory.story"));
		List<Description> descriptions = createDescriptionFrom(story);
		assertEquals(1, descriptions.size());
		Description description = descriptions.get(0);
		Description firstScenario = firstChild(description);
		Description secondScenario = description.getChildren().get(1);
		assertThat(firstChild(firstScenario).getDisplayName(), is(not(firstChild(secondScenario).getDisplayName())));
	}

	@Test
	public void shouldGenerateDescriptionForGivenStories() {
		Scenario scenario = createScenario();
		when(givenStories.getPaths()).thenReturn(singletonList("/some/path/to/GivenStory.story"));
		Description description = createDescriptionFrom(scenario);
		assertThat(firstChild(description), hasProperty("displayName", is("GivenStory\u2024story")));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForExampleTablesOnScenario() {
		int rowsNumber = 2;
		ExamplesTable examplesTable = mockExamplesTable(rowsNumber);
		Scenario scenario = createScenarioWithExamples(examplesTable, GIVEN_STEP);
		Map<String, String> row = examplesTable.getRows().get(0);

		Description description = createDescriptionFrom(scenario);

		assertThat(description.getChildren().size(), is(rowsNumber));
		for (Description exampleDescription : description.getChildren()) {
			assertThat(exampleDescription.getChildren(),
					hasItem(Matchers.<Description>hasProperty("displayName", startsWith(GIVEN_STEP))));
			assertThat(exampleDescription, hasProperty("displayName", startsWith("Example: " + row)));
			// Test is for verifying that the getAnnotations() is successfully executed
			assertThat(exampleDescription.getAnnotations(), is(not(nullValue())));
		}
	}

	@Test
	public void shouldGenerateChildrenForComposedSteps() {
		Scenario scenario = createScenario(GIVEN_STEP);
		String compositeStep1 = "When compositeStep1";
		String compositeStep2 = "When compositeStep2";
		when(stepCandidate.composedSteps()).thenReturn(new String[] { compositeStep1, compositeStep2 });
		StepCandidate composedStep1 = mockStepCandidate(compositeStep1, null);
		StepCandidate composedStep2 = mockStepCandidate(compositeStep2, "When ");
		when(stepCandidate.matches(anyString(), ArgumentMatchers.isNull())).thenReturn(false);
		when(stepCandidate.matches(GIVEN_STEP, null)).thenReturn(true);
		when(stepCandidate.isComposite()).thenReturn(true);
		when(steps.listCandidates()).thenReturn(asList(stepCandidate, composedStep1, composedStep2));

		Description description = createDescriptionFrom(scenario);

		Description composedStep = firstChild(description);
		verify(stepCandidate, times(0)).getStepsInstance();
		assertThat(composedStep.getChildren(),
				everyItem(hasProperty("displayName", startsWith("When compositeStep"))));
		assertEquals(2, composedStep.getChildren().size());
		assertTrue(composedStep.isSuite());
		assertThat(composedStep.getDisplayName(), startsWith(GIVEN_STEP));
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCreateDescriptionForAndStep() {
		Scenario scenario = createScenario(GIVEN_STEP, "And Step2");
		when(stepCandidate.matches(GIVEN_STEP, null)).thenReturn(true);
		when(stepCandidate.matches("And Step2", StepType.GIVEN.toString() + " ")).thenReturn(true);
		when(stepCandidate.getStepType()).thenReturn(StepType.GIVEN);
		when(stepCandidate.getStartingWord()).thenReturn("GIVEN");
		Description description = createDescriptionFrom(scenario);
		assertEquals(2, description.getChildren().size());
	}

	@Test
	public void shouldGenerateDescriptionForPendingSteps() {
		Scenario scenario = createScenario(GIVEN_STEP);
		when(stepCandidate.matches(anyString(), ArgumentMatchers.isNull())).thenReturn(false);
		Description description = createDescriptionFrom(scenario);
		assertEquals(1, description.getChildren().size());
		assertThat(description.getChildren(),
				everyItem(hasProperty("displayName", containsString("PENDING"))));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldSkipExampleTablesForParameterizedGivenStories() {
		// jbehave ignores example tables, when given stories are parameterized
		Scenario scenario = createScenario(GIVEN_STEP);
		when(givenStories.getPaths()).thenReturn(singletonList("/some/path/to/GivenStory.story#{0}"));

		Description description = createDescriptionFrom(scenario);

		assertThat(firstChild(description), hasProperty("displayName", is("GivenStory\u2024story")));
		assertEquals(2, generator.getTestCases());

		assertEquals(2, description.getChildren().size());
		for (Description exampleDescription : description.getChildren()) {
			assertEquals(0, exampleDescription.getChildren().size());
		}
	}

	@Test
	public void shouldExcludeFilteredOutScenarios()  {
		Story story = createStory(createScenario(GIVEN_STEP));
		createDescriptionFrom(true, false, story);
		assertEquals(0, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithAnyType()	{
		mockListBeforeOrAfterScenarioCall(ScenarioType.ANY);
		Scenario scenario = createScenario(GIVEN_STEP);
		createDescriptionFrom(scenario);
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithNormalType()  {
		mockListBeforeOrAfterScenarioCall(ScenarioType.NORMAL);
		Scenario scenario = createScenario(GIVEN_STEP);
		createDescriptionFrom(scenario);
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithAnyAndNormalTypes() {
		mockListBeforeOrAfterScenarioCall(ScenarioType.ANY, ScenarioType.NORMAL);
		Scenario scenario = createScenario(GIVEN_STEP);
		createDescriptionFrom(scenario);
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithExampleType()  {
		mockListBeforeOrAfterScenarioCall(ScenarioType.EXAMPLE);
		Scenario scenario = createScenario(GIVEN_STEP);
		createDescriptionFrom(scenario);
		assertEquals(1, generator.getTestCases());
	}

	private void mockListBeforeOrAfterScenarioCall(ScenarioType... scenarioTypes) {
		Method method = new Object(){}.getClass().getEnclosingMethod();
		for(ScenarioType scenarioType : scenarioTypes) {
			when(steps.listBeforeOrAfterScenario(scenarioType)).thenReturn(
					singletonList(new BeforeOrAfterStep(Stage.BEFORE, method, null)));
		}
	}

	private Description createDescriptionFrom(Scenario scenario) {
		return createDescriptionFrom(true, scenario);
	}

	private Description createDescriptionFrom(boolean allowed, Scenario scenario) {
		PerformableScenario performableScenario = mockPerformableScenario(scenario, null, allowed);
		generator = new JUnitDescriptionGenerator(singletonList(steps), configuration);
		return generator.createDescriptionFrom(Lifecycle.EMPTY, performableScenario);
	}

	private List<Description> createDescriptionFrom(Story story) {
		return createDescriptionFrom(true, true, story);
	}

	private List<Description> createDescriptionFrom(boolean storyAllowed, boolean scenariosAllowed, Story story) {
		List<PerformableScenario> performableScenarios = mockPerformableScenarios(story.getPath(), scenariosAllowed,
				story.getScenarios());

		PerformableStory performableStory = mock(PerformableStory.class);
		when(performableStory.isAllowed()).thenReturn(Boolean.valueOf(storyAllowed));
		when(performableStory.getStory()).thenReturn(story);
		when(performableStory.getScenarios()).thenReturn(performableScenarios);

		List<PerformableStory> performableStories = new ArrayList<>();
		performableStories.add(performableStory);

		PerformableRoot performableRoot = mock(PerformableRoot.class);
		when(performableRoot.getStories()).thenReturn(performableStories);

		PerformableTree performableTree = mock(PerformableTree.class);
		when(performableTree.getRoot()).thenReturn(performableRoot);

		generator = new JUnitDescriptionGenerator(singletonList(steps), configuration);
		return generator.createDescriptionFrom(performableTree);
	}

	private List<PerformableScenario> mockPerformableScenarios(String storyPath, boolean scenariosAllowed,
			List<Scenario> scenarios) {
		List<PerformableScenario> performableScenarios = new ArrayList<>(scenarios.size());
		for (Scenario scenario : scenarios) {
			performableScenarios.add(mockPerformableScenario(scenario, storyPath, scenariosAllowed));
		}
		return performableScenarios;
	}

	private PerformableScenario mockPerformableScenario(Scenario scenario, String storyPath, boolean allowed) {
		PerformableScenario performableScenario = new PerformableScenario(scenario, storyPath);
		performableScenario.allowed(allowed);
		if (scenario.hasExamplesTable()) {
			for (Map<String, String> row : scenario.getExamplesTable().getRows()) {
				ExamplePerformableScenario exampleScenario = mock(ExamplePerformableScenario.class);
				when(exampleScenario.getParameters()).thenReturn(row);
				performableScenario.addExampleScenario(exampleScenario);
			}
		}
		return performableScenario;
	}

	private List<Steps> createBeforeLifecycleSteps() {
		List<Steps> steps = new ArrayList<>();
		for (Scope scope : Scope.values()) {
			steps.add(new Steps(scope, null, singletonList("Then before " + scope)));
		}
		return steps;
	}

	private List<Steps> createAfterLifecycleSteps() {
		List<Steps> steps = new ArrayList<>();
		for (Scope scope : new Scope[] { Scope.SCENARIO, Scope.STORY }) {
			for (Outcome outcome : Outcome.values()) {
				steps.add(new Steps(scope, outcome, singletonList("Then after " + outcome + " " + scope)));
			}
		}
		steps.add(new Steps(Scope.STEP, Outcome.ANY, singletonList("Then after "+ Scope.STEP)));
		return steps;
	}

	private Matcher<Description> whoseDisplayName(Matcher<String> startsWith) {
		return hasProperty("displayName", startsWith);
	}

	private StepCandidate mockStepCandidate(String name, String previousNonAndStep) {
		StepCandidate stepCandidate = mock(StepCandidate.class);
		when(stepCandidate.getStepsType()).then((Answer<Class<?>>) invocation -> STEPS_TYPE);
		when(stepCandidate.matches(name, previousNonAndStep)).thenReturn(true);
		when(stepCandidate.getStartingWord()).thenReturn(name.split(" ")[0]);
		return stepCandidate;
	}

	private Story createStory(Scenario... scenarios) {
		return createStory(Lifecycle.EMPTY, scenarios);
	}

	private Story createStory(Lifecycle lifecycle, Scenario... scenarios) {
		Story story = new Story("storyPath", null, Meta.EMPTY, null, null, lifecycle, asList(scenarios));
		story.namedAs("Default Story Name");
		return story;
	}

	private Scenario createScenario(String... steps) {
		return createScenarioWithExamples(ExamplesTable.EMPTY, steps);
	}

	private Scenario createScenarioWithExamples(ExamplesTable examplesTable, String... steps) {
		return new Scenario(DEFAULT_SCENARIO_TITLE, Meta.EMPTY, givenStories, examplesTable, asList(steps));
	}

	private ExamplesTable mockExamplesTable(int rowsNumber) {
		ExamplesTable examplesTable = mock(ExamplesTable.class);
		Map<String, String> row = new TreeMap<>();
		List<Map<String, String>> rows = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			row.put("key" + i, "value" + i);
		}
		for (int i = 1; i <= rowsNumber; i++) {
			rows.add(row);
		}
		when(examplesTable.getRows()).thenReturn(rows);
		return examplesTable;
	}

	private Description firstChild(Description description) {
		return description.getChildren().get(0);
	}

	private Matcher<List<Description>> allChildrenHaveUniqueDisplayNames() {
		return new BaseMatcher<List<Description>>() {

			private List<Description> descriptions;

			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object descriptions) {
				this.descriptions = (List<Description>) descriptions;
				Set<String> displayNames = new HashSet<>();
				for (Description child : this.descriptions) {
					displayNames.add(child.getDisplayName());
				}
				return displayNames.size() == this.descriptions.size();
			}

			@Override
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
