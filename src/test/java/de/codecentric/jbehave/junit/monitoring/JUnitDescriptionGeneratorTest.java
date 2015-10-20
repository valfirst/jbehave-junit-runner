package de.codecentric.jbehave.junit.monitoring;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class JUnitDescriptionGeneratorTest {

	private static final String DEFAULT_STORY_NAME = "Default Story Name";
	private static final String DEFAULT_SCENARIO_TITLE = "Default Scenario Title";

	@Mock
	private StepCandidate stepCandidate;
	@Mock
	private CandidateSteps steps;
	@Mock
	private Scenario scenario;
	@Mock
	private GivenStories givenStories;
	@Mock
	private Configuration configuration;

	private JUnitDescriptionGenerator generator;
	private Description description;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(steps.listCandidates()).thenReturn(
				Arrays.asList(new StepCandidate[] { stepCandidate }));
		when(stepCandidate.matches(anyString())).thenReturn(true);
		when(stepCandidate.matches(anyString(), anyString())).thenReturn(true);
		when(stepCandidate.getStepsType()).then(returnObjectClass());
		when(scenario.getTitle()).thenReturn(DEFAULT_SCENARIO_TITLE);
		when(givenStories.getPaths()).thenReturn(
				Collections.<String> emptyList());
		when(scenario.getGivenStories()).thenReturn(givenStories);
		when(configuration.keywords()).thenReturn(new Keywords());

		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);
	}

	private PerformableTree mockPerformableTree(Scenario... scenarios) {
		PerformableTree performableTree = mock(PerformableTree.class);
		PerformableRoot performableRoot = mockPerformableRoot(scenarios);
		when(performableTree.getRoot()).thenReturn(performableRoot);
		return performableTree;
	}

	private PerformableRoot mockPerformableRoot(Scenario... scenarios) {
		PerformableRoot performableRoot = mock(PerformableRoot.class);
		List<PerformableStory> performableStories = mockPerformabelStories(scenarios);
		when(performableRoot.getStories()).thenReturn(performableStories);
		return performableRoot;
	}

	private List<PerformableStory> mockPerformabelStories(Scenario... scenarios) {
		List<PerformableStory> performableStories = new ArrayList<>();
		PerformableStory performableStory = mockPerformableStory(true, scenarios);
		performableStories.add(performableStory);
		return performableStories;
	}

	private PerformableStory mockPerformableStory(boolean allowed, Scenario... scenarios) {
		PerformableStory performableStory = mock(PerformableStory.class);
		when(performableStory.isAllowed()).thenReturn(Boolean.valueOf(allowed));
		Story story = mock(Story.class);
		when(story.getName()).thenReturn(DEFAULT_STORY_NAME);
		when(performableStory.getStory()).thenReturn(story);
		when(performableStory.getScenarios()).thenReturn(mockPerformableScenarios(scenarios));
		return performableStory;
	}

	private List<PerformableScenario> mockPerformableScenarios(Scenario... scenarios) {
		List<PerformableScenario> performableScenarios = new ArrayList<>(scenarios.length);
		for (Scenario scenario : scenarios) {
			PerformableScenario performableScenario = new PerformableScenario(scenario, "storyPath");
			performableScenario.allowed(true);
			performableScenarios.add(performableScenario);
		}
		return performableScenarios;
	}

	private Answer<Class<?>> returnObjectClass() {
		return new Answer<Class<?>>() {
			@Override
			public Class<?> answer(InvocationOnMock invocation)
					throws Throwable {
				return Object.class;
			}
		};
	}

	@Test
	public void shouldCountSteps() {
		PerformableTree performableTree = mockPerformableTree(scenario);
		addStepToScenario();
		generator.createDescriptionFrom(performableTree);
		assertEquals(1, generator.getTestCases());
	}

    @Test
	public void shouldExcludeFilteredOutStory() {
		PerformableTree performableTree = mockPerformableTree(scenario);
		addStepToScenario();
		PerformableStory performableStory = mockPerformableStory(false, scenario);
		performableTree.getRoot().getStories().add(performableStory);
		generator.createDescriptionFrom(performableTree);
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldCountIgnorables() {
		PerformableTree performableTree = mockPerformableTree(scenario);
		when(scenario.getSteps()).thenReturn(
				Arrays.asList("Given Step1", "!-- ignore me"));
		when(stepCandidate.matches(anyString(), anyString())).thenReturn(false);
		when(stepCandidate.matches(eq("Given Step1"), anyString())).thenReturn(
				true);
		generator.createDescriptionFrom(performableTree);
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForTopLevelScenario() {
		when(scenario.getTitle()).thenReturn("MyTitle");
		generateScenarioDescription();
		assertThat(
				description,
				equalTo(Description.createSuiteDescription("Scenario: MyTitle")));
		assertThat(generator.getTestCases(), is(0));
	}

	@Test
	public void shouldGenerateDescriptionForStep() {
		addStepToScenario();
		generateScenarioDescription();
		assertThat(description.getChildren(), hasItem(step1Description()));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForTabularParameterStep() {
		when(scenario.getSteps()).thenReturn(
				Arrays.asList("StepWithTableParam:\n|Head|\n|Value|"));
		generateScenarioDescription();
		assertThat(description.getChildren(),
				hasItem(stepWithTableDescription()));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForStory() {
		PerformableTree performableTree = mockPerformableTree(scenario);
		List<Description> descriptions = generator.createDescriptionFrom(performableTree);
		assertEquals(1, descriptions.size());
		assertThat(descriptions.get(0), is(Description.createSuiteDescription(DEFAULT_STORY_NAME)));
	}

	@Test
	public void shouldGenerateDescriptionForScenarioChildOfStory() {
		PerformableTree performableTree = mockPerformableTree(scenario);
		List<Description> descriptions = generator.createDescriptionFrom(performableTree);
		assertEquals(1, descriptions.size());
		assertThat(descriptions.get(0).getChildren(),
				hasItem(Description.createSuiteDescription("Scenario: " + DEFAULT_SCENARIO_TITLE)));
	}

	@Test
	public void shouldStripLinebreaksFromScenarioDescriptions() {
		Scenario scenario = mock(Scenario.class);
		PerformableTree performableTree = mockPerformableTree(scenario);
		when(scenario.getGivenStories()).thenReturn(givenStories);
		when(scenario.getTitle()).thenReturn("Scenario with\nNewline");
		List<Description> descriptions = generator.createDescriptionFrom(performableTree);
		assertEquals(1, descriptions.size());
		assertThat(firstChild(descriptions.get(0)).getDisplayName(), not(containsString("\n")));
	}

	@Test
	public void shouldStripCarriageReturnsFromScenarioDescriptions() {
		Scenario scenario = mock(Scenario.class);
		PerformableTree performableTree = mockPerformableTree(scenario);
		when(scenario.getGivenStories()).thenReturn(givenStories);
		when(scenario.getTitle()).thenReturn("Scenario with\rCarriage Return");
		List<Description> descriptions = generator.createDescriptionFrom(performableTree);
		assertEquals(1, descriptions.size());
		assertThat(firstChild(descriptions.get(0)).getDisplayName(), not(containsString("\r")));
	}

	@Test
	public void shouldCopeWithSeeminglyDuplicateSteps() throws Exception {
		when(scenario.getSteps()).thenReturn(
				Arrays.asList(new String[] { "Given Step1", "Given Step1" }));
		generateScenarioDescription();
		assertThat(description.getChildren(),
				everyItem(whoseDisplayName(startsWith("Given Step1"))));
		assertEquals(2, description.getChildren().size());
		assertThat(description.getChildren(), allChildrenHaveUniqueDisplayNames());
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCopeWithDuplicateGivenStories() throws Exception {
		PerformableTree performableTree = mockPerformableTree(scenario, scenario);
		when(givenStories.getPaths()).thenReturn(
				Arrays.asList("/some/path/to/GivenStory.story"));
		List<Description> descriptions = generator.createDescriptionFrom(performableTree);
		assertEquals(1, descriptions.size());
		Description description = descriptions.get(0);
		Description firstScenario = firstChild(description);
		Description secondScenario = description.getChildren().get(1);
		assertThat(firstChild(firstScenario).getDisplayName(),
				is(not(firstChild(secondScenario).getDisplayName())));
	}

	@Test
	public void shouldGenerateDescriptionForGivenStories() {
		when(givenStories.getPaths()).thenReturn(
				Arrays.asList("/some/path/to/GivenStory.story"));
		generateScenarioDescription();
		assertThat(firstChild(description),
				hasProperty("displayName", is("GivenStory.story")));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForExampleTablesOnScenario() {
		addStepToScenario();
		int NUM_ROWS = 2;
		Map<String, String> row = addExamplesTableToScenario(NUM_ROWS);

		generateScenarioDescription();

		assertThat(description.getChildren().size(), is(NUM_ROWS));
		for (Description exampleDescription : description.getChildren()) {
			assertThat(exampleDescription.getChildren(),
					hasItem(Matchers.<Description> hasProperty("displayName",
							startsWith("Given Step1"))));
			assertThat(exampleDescription,
					hasProperty("displayName", startsWith("Example: " + row)));
		}
	}

	@Test
	public void shouldGenerateChildrenForComposedSteps() {
		addStepToScenario();
		when(stepCandidate.composedSteps()).thenReturn(
				new String[] { "compositeStep1", "compositeStep2" });
		StepCandidate composedStep1 = stepCandidateMock("compositeStep1");
		StepCandidate composedStep2 = stepCandidateMock("compositeStep2");
		when(stepCandidate.matches(anyString(), anyString())).thenReturn(false);
		when(stepCandidate.matches(eq("Given Step1"), anyString())).thenReturn(
				true);
		when(stepCandidate.isComposite()).thenReturn(true);
		when(steps.listCandidates()).thenReturn(
				Arrays.asList(new StepCandidate[] { stepCandidate,
						composedStep1, composedStep2 }));
		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);

		generateScenarioDescription();

		Description composedStep = firstChild(description);
		verify(stepCandidate, times(0)).getStepsInstance();
		assertThat(composedStep.getChildren(),
				everyItem(Matchers.<Description> hasProperty("displayName",
						startsWith("compositeStep"))));
		assertThat(composedStep.getChildren().size(), is(2));
		assertThat(composedStep.isSuite(), is(true));
		assertThat(composedStep.getDisplayName(), startsWith("Given Step1"));
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCreateDescriptionForAndStep() {
		when(scenario.getSteps()).thenReturn(
				Arrays.asList("Given Step1", "And Step2"));
		when(stepCandidate.matches(anyString(), anyString())).thenReturn(false);
		when(stepCandidate.matches(eq("Given Step1"), anyString())).thenReturn(
				true);
		when(
				stepCandidate.matches(eq("And Step2"),
						eq(StepType.GIVEN.toString() + " "))).thenReturn(true);
		when(stepCandidate.getStepType()).thenReturn(StepType.GIVEN);
		when(stepCandidate.getStartingWord()).thenReturn("GIVEN");
		generateScenarioDescription();
		assertThat(description.getChildren().size(), is(2));
	}

	@Test
	public void shouldGenerateDescriptionForPendingSteps() {
		addStepToScenario();
		when(steps.listCandidates()).thenReturn(
				Arrays.asList(new StepCandidate[] { null }));
		when(stepCandidate.matches(anyString())).thenReturn(false);
		when(stepCandidate.matches(anyString(), anyString())).thenReturn(false);
		generateScenarioDescription();
		assertThat(description.getChildren().size(), is(1));
		assertThat(description.getChildren(),
				everyItem(Matchers.<Description> hasProperty("displayName",
						containsString("PENDING"))));
		assertEquals(1, generator.getTestCases());
	}

	@Test
	public void shouldGenerateDescriptionForIgnorableSteps() {
		when(scenario.getSteps()).thenReturn(Arrays.asList("!-- Comment"));

		when(stepCandidate.matches(anyString(), anyString())).thenReturn(false);
		when(
				stepCandidate.matches(eq("!-- Comment"),
						eq(StepType.IGNORABLE.toString() + " "))).thenReturn(
				true);
		when(stepCandidate.getStepType()).thenReturn(StepType.IGNORABLE);
		when(stepCandidate.getStartingWord()).thenReturn("!--");
		generateScenarioDescription();
		assertThat(description.getChildren().size(), is(1));

	}

	@Test
	public void shouldSkipExampleTablesForParameterizedGivenStories() {
		// jbehave ignores example tables, when given stories are parameterized
		addStepToScenario();
		when(givenStories.getPaths()).thenReturn(
				Arrays.asList("/some/path/to/GivenStory.story#{0}"));
		when(givenStories.requireParameters()).thenReturn(true);

		generateScenarioDescription();

		assertThat(firstChild(description),
				hasProperty("displayName", is("GivenStory.story")));
		assertEquals(2, generator.getTestCases());

		assertEquals(2, description.getChildren().size());
		for (Description exampleDescription : description.getChildren()) {
			assertThat(exampleDescription.getChildren().size(), is(0));
		}
	}

	@Test
	public void shouldCountBeforeScenarioStepWithAnyType()	{
		mockListBeforeOrAfterScenarioCall(ScenarioType.ANY);
		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);
		addStepToScenario();
		generateScenarioDescription();
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithNormalType()  {
		mockListBeforeOrAfterScenarioCall(ScenarioType.NORMAL);
		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);
		addStepToScenario();
		generateScenarioDescription();
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithAnyAndNormalTypes() {
		mockListBeforeOrAfterScenarioCall(ScenarioType.ANY, ScenarioType.NORMAL);
		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);
		addStepToScenario();
		generateScenarioDescription();
		assertEquals(2, generator.getTestCases());
	}

	@Test
	public void shouldCountBeforeScenarioStepWithExampleType()  {
		mockListBeforeOrAfterScenarioCall(ScenarioType.EXAMPLE);
		generator = new JUnitDescriptionGenerator(
				Arrays.asList(new CandidateSteps[] { steps }), configuration);
		addStepToScenario();
		generateScenarioDescription();
		assertEquals(1, generator.getTestCases());
	}

	private void mockListBeforeOrAfterScenarioCall(ScenarioType... scenarioTypes) {
		Method method = new Object(){}.getClass().getEnclosingMethod();
		for(ScenarioType scenarioType : scenarioTypes) {
			when(steps.listBeforeOrAfterScenario(scenarioType)).thenReturn(
					Arrays.asList(new BeforeOrAfterStep[] { new BeforeOrAfterStep(Stage.BEFORE, method, null) }));
		}
	}

	private void generateScenarioDescription() {
		PerformableScenario performableScenario = mock(PerformableScenario.class);
		when(performableScenario.getScenario()).thenReturn(scenario);
		description = generator.createDescriptionFrom(performableScenario);
	}

	private Matcher<Description> whoseDisplayName(Matcher<String> startsWith) {
		return Matchers.<Description> hasProperty("displayName", startsWith);
	}

	private StepCandidate stepCandidateMock(String name) {
		StepCandidate step = mock(StepCandidate.class);
		when(step.getStepsType()).then(returnObjectClass());
		when(step.matches(eq(name), anyString())).thenReturn(true);
		return step;
	}

	private void addStepToScenario() {
		when(scenario.getSteps()).thenReturn(Arrays.asList("Given Step1"));
	}

	private Description step1Description() {
		return Description.createTestDescription(Object.class, "Given Step1");
	}

	private Description stepWithTableDescription() {
		return Description.createTestDescription(Object.class,
				"StepWithTableParam:");
	}

	private Map<String, String> addExamplesTableToScenario(int NUM_ROWS) {
		ExamplesTable examplesTable = mock(ExamplesTable.class);
		when(examplesTable.getRowCount()).thenReturn(NUM_ROWS);
		Map<String, String> row = new TreeMap<>();
		List<Map<String, String>> rows = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			row.put("key" + i, "value" + i);
		}
		for (int i = 1; i <= NUM_ROWS; i++) {
			rows.add(row);
		}
		when(examplesTable.getRow(anyInt())).thenReturn(row);
		when(examplesTable.getRows()).thenReturn(rows);
		when(scenario.getExamplesTable()).thenReturn(examplesTable);
		return row;
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
