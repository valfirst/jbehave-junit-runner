package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.ExamplePerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepType;
import org.junit.runner.Description;

public class JUnitDescriptionGenerator {

	public static final String BEFORE_STORY_STEP_NAME = "@BeforeStory";
	public static final String AFTER_STORY_STEP_NAME = "@AfterStory";
	public static final String BEFORE_SCENARIO_STEP_NAME = "@BeforeScenario";
	public static final String AFTER_SCENARIO_STEP_NAME = "@AfterScenario";

	private final DescriptionTextUniquefier uniq = new DescriptionTextUniquefier();
	private int testCases;
	private final List<StepCandidate> allCandidates = new ArrayList<>();

	private final EnumMap<ScenarioType, List<BeforeOrAfterStep>> beforeOrAfterScenario = new EnumMap<>(
			ScenarioType.class);
	{
		for (ScenarioType scenarioType : ScenarioType.values()) {
			beforeOrAfterScenario.put(scenarioType, new ArrayList<>());
		}
	}

	private final List<BeforeOrAfterStep> beforeOrAfterStory = new ArrayList<>();

	private final Configuration configuration;

	private String previousNonAndStep;

	public JUnitDescriptionGenerator(List<CandidateSteps> candidateSteps,
			Configuration configuration) {
		this.configuration = configuration;
		for (CandidateSteps candidateStep : candidateSteps) {
			allCandidates.addAll(candidateStep.listCandidates());
			for (ScenarioType scenarioType : ScenarioType.values()) {
				beforeOrAfterScenario.get(scenarioType).addAll(candidateStep.listBeforeOrAfterScenario(scenarioType));
			}
			beforeOrAfterStory.addAll(candidateStep.listBeforeOrAfterStory(false));
		}
	}

	public List<Description> createDescriptionFrom(PerformableTree performableTree) {
		List<Description> storyDescriptions = new ArrayList<>();
		for (PerformableStory performableStory : performableTree.getRoot().getStories()) {
			if (performableStory.isAllowed()) {
				Story story = performableStory.getStory();
				Lifecycle lifecycle = story.getLifecycle();
				Description storyDescription = createDescriptionForStory(story);
				addBeforeOrAfterStep(Stage.BEFORE, beforeOrAfterStory, storyDescription, BEFORE_STORY_STEP_NAME);
				addSteps(storyDescription, lifecycle.getBeforeSteps(Scope.STORY));
				List<PerformableScenario> scenarios = performableStory.getScenarios();
				for (Description scenarioDescription : getScenarioDescriptions(lifecycle, scenarios)) {
					storyDescription.addChild(scenarioDescription);
				}
				addSteps(storyDescription, lifecycle.getAfterSteps(Scope.STORY, Outcome.ANY));
				addBeforeOrAfterStep(Stage.AFTER, beforeOrAfterStory, storyDescription, AFTER_STORY_STEP_NAME);
				storyDescriptions.add(storyDescription);
			}
		}
		return storyDescriptions;
	}

	public Description createDescriptionFrom(Lifecycle lifecycle, PerformableScenario performableScenario) {
		Scenario scenario = performableScenario.getScenario();
		Description scenarioDescription = createDescriptionForScenario(scenario);
		if (performableScenario.hasExamples() && !scenario.getGivenStories().requireParameters()) {
			insertDescriptionForExamples(lifecycle, performableScenario, scenarioDescription);
		} else {
			if (hasGivenStories(scenario)) {
				insertGivenStories(scenario, scenarioDescription);
			}
			addScenarioSteps(lifecycle, ScenarioType.NORMAL, scenario, scenarioDescription);
		}
		return scenarioDescription;
	}

	private void addScenarioSteps(Lifecycle lifecycle, ScenarioType scenarioType, Scenario scenario, Description scenarioDescription) {
		addBeforeOrAfterScenarioStep(scenarioType, Stage.BEFORE, scenarioDescription, BEFORE_SCENARIO_STEP_NAME);
		addSteps(scenarioDescription, lifecycle.getBeforeSteps(Scope.SCENARIO));
		addScenarioSteps(lifecycle, scenarioDescription, scenario);
		addSteps(scenarioDescription, lifecycle.getAfterSteps(Scope.SCENARIO, Outcome.ANY));
		addBeforeOrAfterScenarioStep(scenarioType, Stage.AFTER, scenarioDescription, AFTER_SCENARIO_STEP_NAME);
	}

	private void addBeforeOrAfterScenarioStep(ScenarioType scenarioType, Stage stage, Description description,
			String stepName) {
		List<BeforeOrAfterStep> beforeOrAfterSteps = new ArrayList<>();
		beforeOrAfterSteps.addAll(beforeOrAfterScenario.get(scenarioType));
		beforeOrAfterSteps.addAll(beforeOrAfterScenario.get(ScenarioType.ANY));
		addBeforeOrAfterStep(stage, beforeOrAfterSteps, description, stepName);
	}

	private void addScenarioSteps(Lifecycle lifecycle, Description scenarioDescription, Scenario scenario) {
		List<String> beforeSteps = lifecycle.getBeforeSteps(Scope.STEP);
		List<String> afterSteps = lifecycle.getAfterSteps(Scope.STEP);
		previousNonAndStep = null;
		String tempPreviousNonAndStep = null;
		for (String scenarioStep : scenario.getSteps()) {
			addSteps(scenarioDescription, beforeSteps);
			previousNonAndStep = tempPreviousNonAndStep;
			addStep(scenarioDescription, scenarioStep);
			tempPreviousNonAndStep = previousNonAndStep;
			addSteps(scenarioDescription, afterSteps);
		}
	}

	private void addBeforeOrAfterStep(Stage stage, List<BeforeOrAfterStep> beforeOrAfterSteps, Description description,
			String stepName)
	{
		for (BeforeOrAfterStep beforeOrAfterStep : beforeOrAfterSteps) {
			if (beforeOrAfterStep.getStage() == stage) {
				testCases++;
				addBeforeOrAfterStep(beforeOrAfterStep, description, stepName);
				break;
			}
		}
	}

	private void addBeforeOrAfterStep(BeforeOrAfterStep beforeOrAfterStep, Description description, String stepName)
	{
		Method method = beforeOrAfterStep.getMethod();
		Description testDescription = Description.createTestDescription(method.getDeclaringClass(),
				getJunitSafeString(stepName), method.getAnnotations());
		description.addChild(testDescription);
	}

	public String getJunitSafeString(String string) {
		return uniq.getUniqueDescription(JUnitStringDecorator.getJunitSafeString(string));
	}

	public int getTestCases() {
		return testCases;
	}

	private boolean hasGivenStories(Scenario scenario) {
		return !scenario.getGivenStories().getPaths().isEmpty();
	}

	private void insertGivenStories(Scenario scenario,
			Description scenarioDescription) {
		for (String path : scenario.getGivenStories().getPaths()) {
			addGivenStoryToScenario(scenarioDescription, path);
		}
	}

	private void addGivenStoryToScenario(Description scenarioDescription,
			String path) {
		scenarioDescription.addChild(Description
				.createSuiteDescription(getJunitSafeString(getFilename(path))));
		testCases++;
	}

	private String getFilename(String path) {
		return path.substring(path.lastIndexOf('/') + 1).split("#")[0];
	}

	private void insertDescriptionForExamples(Lifecycle lifecycle, PerformableScenario performableScenario,
			Description scenarioDescription) {
		Scenario scenario = performableScenario.getScenario();
		for (ExamplePerformableScenario examplePerformableScenario : performableScenario.getExamples()) {
			Description exampleRowDescription = Description.createSuiteDescription(
							configuration.keywords().examplesTableRow() + " " +
									examplePerformableScenario.getParameters());
			scenarioDescription.addChild(exampleRowDescription);
			if (hasGivenStories(scenario)) {
				insertGivenStories(scenario, exampleRowDescription);
			}
			addScenarioSteps(lifecycle, ScenarioType.EXAMPLE, scenario, exampleRowDescription);
		}
	}

	private void addSteps(Description description, List<String> steps) {
		previousNonAndStep = null;
		for (String stringStep : steps) {
			addStep(description, stringStep);
		}
	}

	private void addStep(Description description, String step) {
		String stringStepOneLine = stripLinebreaks(step);
		StepCandidate matchingStep = findMatchingStep(step);
		if (matchingStep == null) {
			addNonExistingStep(description, stringStepOneLine, step);
		} else {
			addExistingStep(description, stringStepOneLine, matchingStep);
		}
	}

	private void addExistingStep(Description description,
			String stringStepOneLine, StepCandidate matchingStep) {
		if (matchingStep.isComposite()) {
			addCompositeSteps(description, stringStepOneLine, matchingStep);
		} else {
			addRegularStep(description, stringStepOneLine, matchingStep);
		}
	}

	private void addNonExistingStep(Description description, String stringStepOneLine,
			String stepAsString) {
		Keywords keywords = configuration.keywords();
		if (keywords.isIgnorableStep(stepAsString)) {
			if (isStep(keywords.stepWithoutStartingWord(stepAsString, StepType.IGNORABLE))) {
				addIgnorableStep(description, stringStepOneLine);
			}
		} else {
			addPendingStep(description, stringStepOneLine);
		}
	}

	private boolean isStep(String stepAsString) {
		Keywords keywords = configuration.keywords();
		for (String stepStartingWord : keywords.startingWordsByType().values()) {
			if (keywords.stepStartsWithWord(stepAsString, stepStartingWord)) {
				return true;
			}
		}
		return false;
	}

	private void addIgnorableStep(Description description, String stringStep) {
		testCases++;
		description.addChild(Description.createSuiteDescription(stringStep));
	}

	private void addPendingStep(Description description, String stringStep) {
		testCases++;
		description.addChild(Description.createSuiteDescription(getJunitSafeString("[PENDING] "
						+ stringStep)));
	}

	private void addRegularStep(Description description, String stringStep,
			StepCandidate step) {
		testCases++;
		// JUnit and the Eclipse JUnit view needs to be touched/fixed in order
		// to make the JUnit view
		// jump to the corresponding test method accordingly. For now we have to
		// live, that we end up in
		// the correct class.
		description.addChild(Description.createTestDescription(step.getStepsType(),
				getJunitSafeString(stringStep)));
	}

	private void addCompositeSteps(Description description, String stringStep,
			StepCandidate step) {
		Description testDescription = Description
				.createSuiteDescription(getJunitSafeString(stringStep));
		addSteps(testDescription, Arrays.asList(step.composedSteps()));
		description.addChild(testDescription);
	}

	private List<Description> getScenarioDescriptions(Lifecycle lifecycle, List<PerformableScenario> performableScenarios) {
		List<Description> scenarioDescriptions = new ArrayList<>();
		for (PerformableScenario scenario : performableScenarios) {
			if (scenario.isAllowed()) {
				scenarioDescriptions.add(createDescriptionFrom(lifecycle, scenario));
			}
		}
		return scenarioDescriptions;
	}

	private StepCandidate findMatchingStep(String stringStep) {
		for (StepCandidate step : allCandidates) {
			if (step.matches(stringStep, previousNonAndStep)) {
				if (step.getStepType() != StepType.AND) {
					previousNonAndStep = step.getStartingWord() + " ";
				}
				return step;
			}
		}
		return null;
	}

	private String stripLinebreaks(String stringStep) {
		if (stringStep.indexOf('\n') != -1) {
			return stringStep.substring(0, stringStep.indexOf('\n'));
		}
		return stringStep;
	}

	private Description createDescriptionForStory(Story story) {
		return Description.createSuiteDescription(getJunitSafeString(story.getName()));
	}

	private Description createDescriptionForScenario(Scenario scenario) {
		return Description.createSuiteDescription(configuration.keywords().scenario() + " "
						+ getJunitSafeString(scenario.getTitle()));
	}
}
