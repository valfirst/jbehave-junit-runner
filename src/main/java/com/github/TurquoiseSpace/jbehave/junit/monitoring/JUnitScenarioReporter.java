package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitScenarioReporter extends NullStoryReporter {
	private final RunNotifier notifier;

	private final Description rootDescription;
	private final int totalTests;
	private final Keywords keywords;
	private final boolean notifyFinished;
	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();

	private final ThreadLocal<TestState> testState = ThreadLocal.withInitial(TestState::new);

	private final AtomicInteger testCounter = new AtomicInteger();

	public JUnitScenarioReporter(RunNotifier notifier, int totalTests,
			Description rootDescription, Keywords keywords,
			boolean notifyFinished) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		this.keywords = keywords;
		this.notifyFinished = notifyFinished;
	}

	public JUnitScenarioReporter(RunNotifier notifier, int totalTests,
			Description rootDescription, Keywords keywords) {
		this(notifier, totalTests, rootDescription, keywords, true);
	}

	@Override
	public void beforeStory(Story story, boolean isGivenStory) {
		TestState testState = this.testState.get();
		if (isGivenStory) {
			if (testState.currentStep != null) {
				notifier.fireTestStarted(testState.currentStep);
			}
			testState.givenStoryLevel++;
		} else {
			if (testCounter.get() == 0) {
				notifier.fireTestRunStarted(rootDescription);
			}
			Description storyDescription = findStoryDescription(story.getName());
			testState.currentStoryDescription = storyDescription;
			notifier.fireTestStarted(storyDescription);
			if (storyDescription.isSuite()) {
				testState.scenarioDescriptions = storyDescription.getChildren().iterator();
				testState.moveToNextScenario();
				processBeforeStory();
			}
			testState.currentStep = testState.currentStoryDescription;
		}
	}

	private Description findStoryDescription(String storyName) {
		String escapedStoryName = JUnitStringDecorator.getJunitSafeString(storyName);
		for (Description storyDescription : rootDescription.getChildren()) {
			if (storyDescription.getDisplayName().equals(escapedStoryName)) {
				return storyDescription;
			} else
			// Related to issue #28: When a story does not contain any scenarios, isTest returns true,
			// but getMethodName still returns null, because it cannot be parsed by JUnit as a method name.
			if (storyDescription.isTest() && storyDescription.getMethodName() != null
					&& storyDescription.getMethodName().equals(storyName)) {
				// Story BeforeStories or AfterStories
				return storyDescription;
			}
		}
		throw new IllegalStateException("No JUnit description found for story with name: " + storyName);
	}

	@Override
	public void afterStory(boolean isGivenStory) {
		TestState testState = this.testState.get();
		if (isGivenStory) {
			testState.givenStoryLevel--;
			if (testState.currentStep != null) {
				notifier.fireTestFinished(testState.currentStep);
			}
			prepareNextStep();
			processBeforeScenario();
		} else {
			if (!testState.failedSteps.contains(testState.currentStoryDescription)) {
				notifier.fireTestFinished(testState.currentStoryDescription);
				if (testState.currentStoryDescription.isTest()) {
					testCounter.incrementAndGet();
				}
			}
			processAfterStory();
			if (testCounter.get() == totalTests && notifyFinished) {
				Result result = new Result();
				notifier.fireTestRunFinished(result);
			}
		}
	}

	@Override
	public void beforeScenario(Scenario scenario) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			notifier.fireTestStarted(testState.currentScenario);

			List<Description> children = testState.currentScenario.getChildren();
			List<Description> examples = filterExamples(children);
			if (!examples.isEmpty()) {
				testState.exampleDescriptions = examples.iterator();
				testState.currentExample = null;
			}
			if (children.size() > examples.size()) {
				// in case of given stories, these steps are actually stories,
				// for which events will be fired in beforeStory(..., true)
				List<Description> steps = new ArrayList<>(
						testState.currentScenario.getChildren());
				steps.removeAll(examples);
				testState.loadStepDescriptions(steps);
				testState.moveToNextStep();
				processBeforeScenario();
			}
		}
	}

	private List<Description> filterExamples(List<Description> children) {
		for (int i = 0; i < children.size(); i++) {
			Description child = children.get(i);
			boolean isExample = child.getDisplayName().startsWith(
					keywords.examplesTableRow() + " ");
			if (isExample) {
				return children.subList(i, children.size());
			}
		}
		return Collections.emptyList();
	}

	public void afterScenario() {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			notifier.fireTestFinished(testState.currentScenario);
			processAfterScenario();
			testState.moveToNextScenario();
		}
	}

	private void processBeforeStory() {
		TestState testState = this.testState.get();
		Description currentScenario = testState.currentScenario;
		if (currentScenario != null &&
				currentScenario.getDisplayName().startsWith(JUnitDescriptionGenerator.BEFORE_STORY_STEP_NAME)) {
			// @BeforeStory has been called already
			notifier.fireTestStarted(currentScenario);
			notifier.fireTestFinished(currentScenario);
			testState.moveToNextScenario();
		}
	}

	private void processAfterStory() {
		TestState testState = this.testState.get();
		Description currentScenario = testState.currentScenario;
		if (currentScenario != null) {
			if (currentScenario.getDisplayName().startsWith(JUnitDescriptionGenerator.AFTER_STORY_STEP_NAME)) {
				// @AfterStory has been called already
				notifier.fireTestStarted(currentScenario);
				notifier.fireTestFinished(currentScenario);
				testState.moveToNextScenario();
			}
			else {
				testState.moveToNextScenario();
				processAfterStory();
			}
		}
	}

	private void processBeforeScenario() {
		Description currentStep = testState.get().currentStep;
		if (currentStep != null &&
				currentStep.getDisplayName().startsWith(JUnitDescriptionGenerator.BEFORE_SCENARIO_STEP_NAME)) {
			// @BeforeScenario has been called already
			notifier.fireTestStarted(currentStep);
			notifier.fireTestFinished(currentStep);
			prepareNextStep();
		}
	}

	private void processAfterScenario() {
		TestState testState = this.testState.get();
		Description currentStep = testState.currentStep;
		if (currentStep != null) {
			if (currentStep.getDisplayName().startsWith(JUnitDescriptionGenerator.AFTER_SCENARIO_STEP_NAME)) {
				// @AfterScenario has been called already
				notifier.fireTestStarted(currentStep);
				notifier.fireTestFinished(currentStep);
				prepareNextStep();
			}
			else {
				testState.moveToNextStep();
				processAfterScenario();
			}
		}
	}

	@Override
	public void example(Map<String, String> tableRow, int exampleIndex) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			if (testState.currentExample != null && testState.stepDescriptions != null) {
				processAfterScenario();
			}
			testState.moveToNextExample();
			testState.loadStepDescriptions(testState.currentExample.getChildren());
			testState.moveToNextStep();
			processBeforeScenario();
		}
	}

	public void beforeStep(String title) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning() && testState.currentStep != null) {
			// Lifecycle Before story steps
			if (testState.currentStep == testState.currentStoryDescription) {
				testState.currentStep = testState.currentScenario;
			}
			if (testState.currentStepStatus == StepStatus.STARTED) {
				testState.parentSteps.push(testState.currentStep);
				// Composite Lifecycle Before/After story steps
				if (testState.stepDescriptions == null) {
					testState.loadStepDescriptions(testState.currentStep.getChildren());
				}
				testState.moveToNextStep();
			}
			notifier.fireTestStarted(testState.currentStep);
			testState.currentStepStatus = StepStatus.STARTED;
		}
	}

	@Override
	public void failed(String step, Throwable e) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			Throwable thrownException = e instanceof UUIDExceptionWrapper ? e.getCause() : e;
			if (thrownException instanceof BeforeOrAfterFailed) {
				notifier.fireTestStarted(testState.currentStep);
			}
			notifier.fireTestFailure(new Failure(testState.currentStep, thrownException));
			testState.failedSteps.add(testState.currentStep);
			finishStep(testState);
		}
	}

	@Override
	public void successful(String step) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			if (testState.currentStep != null) {
			    finishStep(testState);
			} else {
			    prepareNextStep();
			}
		}
	}

	private void prepareNextStep() {
		TestState testState = this.testState.get();
		if (testState.currentStep != null) {
			if (testState.currentStep.isTest()) {
				testCounter.incrementAndGet();
			}
			// Lifecycle Before/After story steps
			if (testState.currentStep == testState.currentScenario || !testState.parentSteps.isEmpty()
					&& testState.parentSteps.peekLast() == testState.currentScenario) {
				testState.moveToNextScenario();
				return;
			}
		}
		if (testState.stepDescriptions != null) {
			testState.moveToNextStep();
		}
	}

	private void finishStep(TestState testState) {
		if (testState.currentStepStatus == StepStatus.FINISHED && !testState.parentSteps.isEmpty()) {
			notifier.fireTestFinished(testState.parentSteps.poll());
		} else {
			notifier.fireTestFinished(testState.currentStep);
			testState.currentStepStatus = StepStatus.FINISHED;
			prepareNextStep();
		}
	}

	@Override
	public void pending(String step) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			if (pendingStepStrategy instanceof FailingUponPendingStep) {
				notifier.fireTestStarted(testState.currentStep);
				notifier.fireTestFailure(new Failure(testState.currentStep,
						new RuntimeException("Step is pending!")));
				// Pending step strategy says to fail so treat this step as
				// having failed.
				testState.failedSteps.add(testState.currentStep);
				finishStep(testState);
			} else {
				notifier.fireTestIgnored(testState.currentStep);
				prepareNextStep();
			}
		}
	}

	@Override
	public void ignorable(String step) {
		TestState testState = this.testState.get();
		if (!testState.isGivenStoryRunning()) {
			notifier.fireTestIgnored(testState.currentStep);
			testState.currentStepStatus = StepStatus.FINISHED;
			prepareNextStep();
		}
	}

	@Override
	public void notPerformed(String step) {
		ignorable(step);
	}

	/**
	 * Notify the IDE that the current step and scenario is not being executed.
	 * Reason is a JBehave meta tag is filtering out this scenario.
	 *
	 * @param scenario Scenario
	 * @param filter Filter
	 */
	public void scenarioNotAllowed(Scenario scenario, String filter) {
		TestState testState = this.testState.get();
		notifier.fireTestIgnored(testState.currentStep);
		notifier.fireTestIgnored(testState.currentScenario);
	}

	public void usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
		this.pendingStepStrategy = pendingStepStrategy;
	}

	private class TestState {
		private Description currentStep;
		private StepStatus currentStepStatus;
		private final Deque<Description> parentSteps = new LinkedList<>();
		private Iterator<Description> stepDescriptions;

		private Description currentScenario;
		private Iterator<Description> scenarioDescriptions;

		private Description currentExample;
		private Iterator<Description> exampleDescriptions;

		private Description currentStoryDescription;
		private int givenStoryLevel;

		private final Set<Description> failedSteps = new HashSet<>();

		private void moveToNextScenario() {
			currentScenario = getNextOrNull(scenarioDescriptions);
			currentStep = currentScenario;
			stepDescriptions = null;
		}

		private void moveToNextExample() {
			currentExample = getNextOrNull(exampleDescriptions);
		}

		private void moveToNextStep() {
			currentStep = getNextOrNull(stepDescriptions);
		}

		private boolean isGivenStoryRunning() {
			return givenStoryLevel != 0;
		}

		private void loadStepDescriptions(List<Description> steps) {
			stepDescriptions = getAllDescendants(steps).iterator();
		}

		private <T> T getNextOrNull(Iterator<T> iterator) {
			return iterator.hasNext() ? iterator.next() : null;
		}

		private Collection<Description> getAllDescendants(List<Description> steps) {
			List<Description> descendants = new ArrayList<>();
			for (Description child : steps) {
				descendants.add(child);
				descendants.addAll(getAllDescendants(child.getChildren()));
			}
			return descendants;
		}
	}

	private enum StepStatus {
		STARTED, FINISHED
	}
}
