package de.codecentric.jbehave.junit.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
	private Logger logger = new Logger();

	private RunNotifier notifier;

	private final Description rootDescription;
	private final int totalTests;
	private final Keywords keywords;
	private final boolean notifyFinished;
	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();

	private ThreadLocal<TestState> testState = new ThreadLocal<TestState>() {
		@Override
		protected TestState initialValue() {
			return new TestState();
		}
	};

	private AtomicInteger testCounter = new AtomicInteger();

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
		logger.info("Before Story: {} {}", story.getName(),
				isGivenStory ? "(given story)" : "");
		TestState testState = this.testState.get();
		if (isGivenStory) {
			if (testState.currentStep != null) {
				notifier.fireTestStarted(testState.currentStep);
			}
			testState.givenStoryContext = true;
		} else {
			if (testCounter.get() == 0) {
				notifier.fireTestRunStarted(rootDescription);
			}
			for (Description storyDescription : rootDescription.getChildren()) {
				if (storyDescription.isSuite()
						&& storyDescription.getDisplayName().equals(
								JUnitStringDecorator.getJunitSafeString(story.getName()))) {
					testState.currentStoryDescription = storyDescription;
					notifier.fireTestStarted(storyDescription);

					testState.scenarioDescriptions = storyDescription.getChildren().iterator();
					testState.moveToNextScenario();
					processBeforeStory();
					testState.currentStep = testState.currentStoryDescription;
				} else
				// Related to issue #28: When a story does not contain any
				// scenarios, isTest returns true, but getMethodName
				// still returns null, because it cannot be parsed by JUnit as a
				// method name.
				if (storyDescription.isTest()
						&& storyDescription.getMethodName() != null
						&& storyDescription.getMethodName().equals(
								story.getName())) {
					// Story BeforeStories or After Stories
					testState.currentStoryDescription = storyDescription;
					notifier.fireTestStarted(testState.currentStoryDescription);
					testState.currentStep = testState.currentStoryDescription;
				}
			}
		}
	}

	@Override
	public void afterStory(boolean isGivenStory) {
		TestState testState = this.testState.get();
		logger.info("After Story: {} {}", testState.currentStoryDescription
				.getDisplayName(), isGivenStory ? "(given story)" : "");
		if (isGivenStory) {
			testState.givenStoryContext = false;
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
	public void beforeScenario(String title) {
		logger.info("Before Scenario: {}", title);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
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
				testState.stepDescriptions = getAllDescendants(steps).iterator();
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

	private Collection<Description> getAllDescendants(List<Description> steps) {
		List<Description> descendants = new ArrayList<>();
		for (Description child : steps) {
			descendants.add(child);
			descendants.addAll(getAllDescendants(child.getChildren()));
		}
		return descendants;
	}

	@Override
	public void afterScenario() {
		TestState testState = this.testState.get();
		logger.info("After Scenario: {}", testState.currentScenario.getDisplayName());
		if (!testState.givenStoryContext) {
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
	public void example(Map<String, String> arg0) {
		logger.info("Example: {}", arg0);

		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			if (testState.currentExample != null && testState.stepDescriptions != null) {
				processAfterScenario();
			}
			testState.moveToNextExample();
			testState.stepDescriptions = testState.currentExample.getChildren().iterator();
			testState.moveToNextStep();
			processBeforeScenario();
		}
	}

	@Override
	public void beforeStep(String title) {
		logger.info("Before Step: {}", title);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			notifier.fireTestStarted(testState.currentStep);
		}
	}

	@Override
	public void failed(String step, Throwable e) {
		if (e instanceof UUIDExceptionWrapper) {
			e = e.getCause();
		}
		logger.info("Step Failed: {} (cause: {})", step, e.getMessage());
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			if (e instanceof BeforeOrAfterFailed) {
				notifier.fireTestStarted(testState.currentStep);
			}
			notifier.fireTestFailure(new Failure(testState.currentStep, e));
			notifier.fireTestFinished(testState.currentStep);
			testState.failedSteps.add(testState.currentStep);
			prepareNextStep();
		}
	}

	@Override
	public void successful(String step) {
		logger.info("Step Succesful: {}", step);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			notifier.fireTestFinished(testState.currentStep);

			prepareNextStep();
		}
	}

	private void prepareNextStep() {
		TestState testState = this.testState.get();
		if (testState.currentStep != null && testState.currentStep.isTest()) {
			testCounter.incrementAndGet();
		}
		if (testState.stepDescriptions != null) {
			testState.moveToNextStep();
		}
	}

	@Override
	public void pending(String arg0) {
		logger.info("Pending: {}", arg0);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			if (pendingStepStrategy instanceof FailingUponPendingStep) {
				notifier.fireTestStarted(testState.currentStep);
				notifier.fireTestFailure(new Failure(testState.currentStep,
						new RuntimeException("Step is pending!")));
				// Pending step strategy says to fail so treat this step as
				// having failed.
				testState.failedSteps.add(testState.currentStep);
				notifier.fireTestFinished(testState.currentStep);
			} else {
				notifier.fireTestIgnored(testState.currentStep);
			}

			prepareNextStep();
		}
	}

	@Override
	public void ignorable(String arg0) {
		logger.info("Ignorable: {}", arg0);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			notifier.fireTestIgnored(testState.currentStep);
			prepareNextStep();
		}
	}

	@Override
	public void notPerformed(String arg0) {
		logger.info("Not performed: {}", arg0);
		TestState testState = this.testState.get();
		if (!testState.givenStoryContext) {
			notifier.fireTestIgnored(testState.currentStep);
			prepareNextStep();
		}
	}

	/**
	 * Notify the IDE that the current step and scenario is not being executed.
	 * Reason is a JBehave meta tag is filtering out this scenario.
	 *
	 * @param scenario Scenario
	 * @param filter Filter
	 */
	@Override
	public void scenarioNotAllowed(Scenario scenario, String filter) {
		logger.info("Scenario not allowed: {} {}", scenario, filter);
		TestState testState = this.testState.get();
		notifier.fireTestIgnored(testState.currentStep);
		notifier.fireTestIgnored(testState.currentScenario);
	}

	public void usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
		this.pendingStepStrategy = pendingStepStrategy;
	}

	private class TestState {
		private Description currentStep;
		private Iterator<Description> stepDescriptions;

		private Description currentScenario;
		private Iterator<Description> scenarioDescriptions;

		private Description currentExample;
		private Iterator<Description> exampleDescriptions;

		private Description currentStoryDescription;
		private boolean givenStoryContext;

		private Set<Description> failedSteps = new HashSet<>();

		private void moveToNextScenario() {
			currentScenario = getNextOrNull(scenarioDescriptions);
			currentStep = currentScenario;
		}

		private void moveToNextExample() {
			currentExample = getNextOrNull(exampleDescriptions);
		}

		private void moveToNextStep() {
			currentStep = getNextOrNull(stepDescriptions);
		}

		private <T> T getNextOrNull(Iterator<T> iterator) {
			return iterator.hasNext() ? iterator.next() : null;
		}
	}
}
