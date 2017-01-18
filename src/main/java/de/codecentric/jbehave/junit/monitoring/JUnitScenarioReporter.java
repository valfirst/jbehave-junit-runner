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
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitScenarioReporter implements ExtendedStoryReporter {
	private static Logger logger = LoggerFactory.getLogger(JUnitScenarioReporter.class);

	private RunNotifier notifier;
	private Description currentScenario;
	private Description currentStep;
	private Iterator<Description> scenarioDescriptions;
	private final Description rootDescription;
	private final ArrayList<Description> storyDescriptions;

	private Description currentStoryDescription;
	private Iterator<Description> stepDescriptions;
	private Iterator<Description> exampleDescriptions;
	private Description nextExample;
	int testCounter = 0;
	private final int totalTests;

	private boolean givenStoryContext;
	public Set<Description> failedSteps = new HashSet<Description>();

	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();
	private Keywords keywords;

	private boolean notifyFinished = true;

	public JUnitScenarioReporter(RunNotifier notifier, int totalTests,
			Description rootDescription, Keywords keywords) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		this.storyDescriptions = rootDescription.getChildren();
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
			if (testCounter == 0) {
				notifier.fireTestRunStarted(rootDescription);
			}
			for (Description storyDescription : storyDescriptions) {
				if (storyDescription.isSuite()
						&& storyDescription.getDisplayName().equals(
								story.getName())) {
					currentStoryDescription = storyDescription;
					notifier.fireTestStarted(storyDescription);

					scenarioDescriptions = storyDescription.getChildren()
							.iterator();
					if (scenarioDescriptions.hasNext()) {
						currentScenario = scenarioDescriptions.next();
					}
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
		} else {
			if (!failedSteps.contains(currentStoryDescription)) {
				notifier.fireTestFinished(currentStoryDescription);
				if (currentStoryDescription.isTest())
					testCounter++;
			}

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

			ArrayList<Description> children = currentScenario.getChildren();
			List<Description> examples = filterExamples(children);
			if (!examples.isEmpty()) {
				exampleDescriptions = examples.iterator();
				if (exampleDescriptions.hasNext()) {
					nextExample = exampleDescriptions.next();
				}
			}
			if (children.size() > examples.size()) {
				// in case of given stories, these steps are actually stories,
				// for which events
				// will be fired in beforeStory(..., true)
				ArrayList<Description> steps = new ArrayList<Description>(
						currentScenario.getChildren());
				steps.removeAll(examples);
				stepDescriptions = getAllDescendants(steps).iterator();
				if (stepDescriptions.hasNext()) {
					currentStep = stepDescriptions.next();
				}
			}
		}
	}

	private List<Description> filterExamples(ArrayList<Description> children) {
		for (int i = 0; i < children.size(); i++) {
			Description child = (Description) children.get(i);
			boolean isExample = child.getDisplayName().startsWith(
					keywords.examplesTableRow() + " ");
			if (isExample) {
				return children.subList(i, children.size());
			}
		}
		return Collections.emptyList();
	}

	private Collection<Description> getAllDescendants(
			ArrayList<Description> steps) {
		List<Description> descendants = new ArrayList<Description>();
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
			notifier.fireTestFinished(currentScenario);
			if (scenarioDescriptions.hasNext()) {
				currentScenario = scenarioDescriptions.next();
				logger.debug("--> updating current scenario to {}",
						currentScenario.getDisplayName());
			}
		}
	}

	@Override
	public void beforeExamples(List<String> arg0, ExamplesTable arg1) {
		logger.info("Before Examples: {}", arg0 != null ? arg0 : "n/a");
	}

	@Override
	public void example(Map<String, String> arg0) {
		logger.info("Example: {}", arg0);

		stepDescriptions = nextExample.getChildren().iterator();
		if (stepDescriptions.hasNext()) {
			currentStep = stepDescriptions.next();
		}

		if (exampleDescriptions.hasNext()) {
			nextExample = exampleDescriptions.next();
		}

	}

	@Override
	public void afterExamples() {
		logger.info("{}", "afterExamples");

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

	private void prepareNextStep() {
		if (currentStep.isTest())
			testCounter++;
		if (stepDescriptions != null && stepDescriptions.hasNext()) {
			currentStep = stepDescriptions.next();
		}
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
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void scenarioNotAllowed(Scenario arg0, String arg1) {
		logger.info("Scenario not allowed: {} {}", arg0, arg1);
		notifier.fireTestIgnored(currentStep);
		notifier.fireTestIgnored(currentScenario);
	}

	// BASICALLY UN-IMPLEMENTED METHODS

	@Override
	public void dryRun() {
		logger.info("{}", "dryRun");
	}

	@Override
	public void failedOutcomes(String arg0, OutcomesTable arg1) {
		logger.info("Failed outcomes: {}", arg0);
	}

	@Override
	public void givenStories(GivenStories arg0) {
		logger.info("Given Stories: {}", arg0);
	}

	@Override
	public void givenStories(List<String> arg0) {
		logger.info("Given Stories (List): {}", arg0);
	}

	@Override
	public void narrative(Narrative arg0) {
		logger.info("Narrative: {}", arg0);
	}

	@Override
	public void pendingMethods(List<String> arg0) {
		logger.info("Pending methods: {}", arg0);
	}

	@Override
	public void restarted(String arg0, Throwable arg1) {
		logger.info("Restarted: {} ({})", arg0, arg1);
	}

	@Override
	public void scenarioMeta(Meta arg0) {
		logger.info("Meta: {}", arg0);
	}

	@Override
	public void storyCancelled(Story arg0, StoryDuration arg1) {
		logger.info("Story cancelled: {} after {}", arg0, arg1);
		System.out.println("JBehave2JunitReporter.storyCancelled()");
	}

	@Override
	public void storyNotAllowed(Story arg0, String arg1) {
		logger.info("Story not allowed: {}, {}", arg0, arg1);
	}

	@Override
	public void usePendingStepStrategy(PendingStepStrategy strategy) {
		this.pendingStepStrategy = strategy;
	}

	@Override
	public void lifecyle(Lifecycle lifecycle) {
		logger.info("Story lifecycle: {}", lifecycle);
	}

}
