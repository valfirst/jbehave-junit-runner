package de.codecentric.jbehave.junit.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	Logger logger = new Logger();

	private RunNotifier notifier;
	private Description currentScenario;
	private Description currentStep;
	private Iterator<Description> scenarioDescriptions;
	private final Description rootDescription;
	private final ArrayList<Description> storyDescriptions;

	private Description currentStoryDescription;
	private Iterator<Description> stepDescriptions;
	private Iterator<Description> exampleDescriptions;
	private Description currentExample;
	int testCounter = 0;
	private final int totalTests;

	private boolean givenStoryContext;
	public Set<Description> failedSteps = new HashSet<>();

	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();
	private Keywords keywords;

	/**
	 * Use to track whether any scenario in the current story has failed. This
	 * is useful to indicate a story has failed.
	 */
	private boolean anyScenarioFailedInCurrentStory = false;

	private boolean notifyFinished = true;

	public JUnitScenarioReporter(RunNotifier notifier, int totalTests,
			Description rootDescription, Keywords keywords,
			boolean notifyFinished) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		storyDescriptions = rootDescription.getChildren();
		this.keywords = keywords;
		this.notifyFinished = notifyFinished;
	}

	public JUnitScenarioReporter(RunNotifier notifier, int totalTests,
			Description rootDescription, Keywords keywords) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		storyDescriptions = rootDescription.getChildren();
		this.keywords = keywords;
	}

	@Override
	public void beforeStory(Story story, boolean isGivenStory) {
		logger.info("Before Story: {} {}", story.getName(),
				isGivenStory ? "(given story)" : "");
		if (isGivenStory) {
			notifier.fireTestStarted(currentStep);
			givenStoryContext = true;

		} else {
			anyScenarioFailedInCurrentStory = false;
			if (testCounter == 0) {
				notifier.fireTestRunStarted(rootDescription);
			}
			for (Description storyDescription : storyDescriptions) {
				if (storyDescription.isSuite()
						&& storyDescription.getDisplayName().equals(
								JUnitStringDecorator.getJunitSafeString(story.getName()))) {
					currentStoryDescription = storyDescription;
					notifier.fireTestStarted(storyDescription);

					scenarioDescriptions = storyDescription.getChildren()
							.iterator();
					moveToNextScenario();
					processBeforeStory();
					currentStep = currentStoryDescription;
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
					currentStoryDescription = storyDescription;
					notifier.fireTestStarted(currentStoryDescription);
					currentStep = currentStoryDescription;
				}
			}
		}

	}

	@Override
	public void afterStory(boolean isGivenStory) {
		logger.info("After Story: {} {}", currentStoryDescription
				.getDisplayName(), isGivenStory ? "(given story)" : "");
		if (isGivenStory) {
			givenStoryContext = false;
			notifier.fireTestFinished(currentStep);
			prepareNextStep();
			processBeforeScenario();
		} else {
			if (!failedSteps.contains(currentStoryDescription)) {
				// IntelliJ 13.1 does not propogate a step failure up to the
				// story level.
				// When there is a step failure then notify that its story has
				// also failed.
				if (anyScenarioFailedInCurrentStory == false) {
					notifier.fireTestFinished(currentStoryDescription);
				} else {
					notifier.fireTestFailure(new Failure(
							currentStoryDescription, new RuntimeException(
									"story failed!")));
				}
				if (currentStoryDescription.isTest())
				{
					testCounter++;
				}
			}
			processAfterStory();
			if (testCounter == totalTests && notifyFinished) {
				Result result = new Result();
				notifier.fireTestRunFinished(result);
			}
		}
	}

	@Override
	public void beforeScenario(String title) {
		logger.info("Before Scenario: {}", title);
		if (!givenStoryContext) {
			notifier.fireTestStarted(currentScenario);

			List<Description> children = currentScenario.getChildren();
			List<Description> examples = filterExamples(children);
			if (!examples.isEmpty()) {
				exampleDescriptions = examples.iterator();
				currentExample = null;
			}
			if (children.size() > examples.size()) {
				// in case of given stories, these steps are actually stories,
				// for which events will be fired in beforeStory(..., true)
				List<Description> steps = new ArrayList<>(
						currentScenario.getChildren());
				steps.removeAll(examples);
				stepDescriptions = getAllDescendants(steps).iterator();
				moveToNextStep();
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

	private Collection<Description> getAllDescendants(
			List<Description> steps) {
		List<Description> descendants = new ArrayList<>();
		for (Description child : steps) {
			descendants.add(child);
			descendants.addAll(getAllDescendants(child.getChildren()));
		}
		return descendants;
	}

	@Override
	public void afterScenario() {
		logger.info("After Scenario: {}", currentScenario.getDisplayName());
		if (!givenStoryContext) {
			// IntelliJ 13.1 does not propogate a step failure up to the
			// scenario level.
			// When there is a step failure then notify that its scenario has
			// also failed.
			if (failedSteps.size() == 0) {
				notifier.fireTestFinished(currentScenario);
			} else {
				notifier.fireTestFailure(new Failure(currentScenario,
						new RuntimeException("scenario failed!")));
				anyScenarioFailedInCurrentStory = true;
				// TODO: Code review this. Do we really need to keep track of
				// failed steps between scenarios?
				failedSteps.clear();
			}
			processAfterScenario();
			moveToNextScenario();
		}
	}

	private void processBeforeStory() {
		if (currentScenario != null &&
				currentScenario.getDisplayName().startsWith(JUnitDescriptionGenerator.BEFORE_STORY_STEP_NAME)) {
			// @BeforeStory has been called already
			notifier.fireTestStarted(currentScenario);
			notifier.fireTestFinished(currentScenario);
			moveToNextScenario();
		}
	}

	private void processAfterStory() {
		if (currentScenario != null) {
			if (currentScenario.getDisplayName().startsWith(JUnitDescriptionGenerator.AFTER_STORY_STEP_NAME)) {
				// @AfterStory has been called already
				notifier.fireTestStarted(currentScenario);
				notifier.fireTestFinished(currentScenario);
				moveToNextScenario();
			}
			else {
				moveToNextScenario();
				processAfterStory();
			}
		}
	}

	private void processBeforeScenario() {
		if (currentStep != null &&
				currentStep.getDisplayName().startsWith(JUnitDescriptionGenerator.BEFORE_SCENARIO_STEP_NAME)) {
			// @BeforeScenario has been called already
			notifier.fireTestStarted(currentStep);
			notifier.fireTestFinished(currentStep);
			prepareNextStep();
		}
	}

	private void processAfterScenario() {
		if (currentStep != null) {
			if (currentStep.getDisplayName().startsWith(JUnitDescriptionGenerator.AFTER_SCENARIO_STEP_NAME)) {
				// @AfterScenario has been called already
				notifier.fireTestStarted(currentStep);
				notifier.fireTestFinished(currentStep);
				prepareNextStep();
			}
			else {
				moveToNextStep();
				processAfterScenario();
			}
		}
	}

	@Override
	public void example(Map<String, String> arg0) {
		logger.info("Example: {}", arg0);

		if (currentExample != null && stepDescriptions != null)
		{
			processAfterScenario();
		}
		moveToNextExample();
		stepDescriptions = currentExample.getChildren().iterator();
		moveToNextStep();
		processBeforeScenario();
	}

	private void moveToNextExample()
	{
		currentExample = exampleDescriptions.hasNext() ? exampleDescriptions.next() : null;
	}

	@Override
	public void beforeStep(String title) {
		logger.info("Before Step: {}", title);
		if (!givenStoryContext) {
			notifier.fireTestStarted(currentStep);
		}
	}

	@Override
	public void failed(String step, Throwable e) {
		if (e instanceof UUIDExceptionWrapper) {
			e = ((UUIDExceptionWrapper) e).getCause();
		}
		logger.info("Step Failed: {} (cause: {})", step, e.getMessage());
		if (!givenStoryContext) {
			if (e instanceof BeforeOrAfterFailed)
			{
				notifier.fireTestStarted(currentStep);
			}
			notifier.fireTestFailure(new Failure(currentStep, e));
			notifier.fireTestFinished(currentStep);
			failedSteps.add(currentStep);
			prepareNextStep();
		}
	}

	@Override
	public void successful(String step) {
		logger.info("Step Succesful: {}", step);
		if (!givenStoryContext) {
			notifier.fireTestFinished(currentStep);

			prepareNextStep();
		}
	}

	private void moveToNextScenario()
	{
		currentScenario = scenarioDescriptions.hasNext() ? scenarioDescriptions.next() : null;
		currentStep = currentScenario;
	}

	private void prepareNextStep() {
		if (currentStep != null && currentStep.isTest())
		{
			testCounter++;
		}
		if (stepDescriptions != null) {
			moveToNextStep();
		}
	}

	 private void moveToNextStep() {
		currentStep = stepDescriptions.hasNext() ? stepDescriptions.next() : null;
	}

	@Override
	public void pending(String arg0) {
		logger.info("Pending: {}", arg0);
		if (!givenStoryContext) {
			if (pendingStepStrategy instanceof FailingUponPendingStep) {
				notifier.fireTestStarted(currentStep);
				notifier.fireTestFailure(new Failure(currentStep,
						new RuntimeException("Step is pending!")));
				// Pending step strategy says to fail so treat this step as
				// having failed.
				failedSteps.add(currentStep);
				notifier.fireTestFinished(currentStep);
			} else {
				notifier.fireTestIgnored(currentStep);
			}

			prepareNextStep();
		}
	}

	@Override
	public void ignorable(String arg0) {
		logger.info("Ignorable: {}", arg0);
		if (!givenStoryContext) {
			notifier.fireTestIgnored(currentStep);
			prepareNextStep();
		}
	}

	@Override
	public void notPerformed(String arg0) {
		logger.info("Not performed: {}", arg0);
		if (!givenStoryContext) {
			notifier.fireTestIgnored(currentStep);
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
		notifier.fireTestIgnored(currentStep);
		notifier.fireTestIgnored(currentScenario);
	}

	public void usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
		this.pendingStepStrategy = pendingStepStrategy;
	}
}
