package de.codecentric.jbehave.junit.monitoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jbehave.core.reporters.StoryReporter;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link StoryReporter}. 
 * This reporter reports only story level events.
 * 
 * @author Michal Bocek
 * @since 27/07/16 
 */
public class JUnitStoryReporter implements ExtendedStoryReporter {
	private static Logger logger = LoggerFactory.getLogger(JUnitStoryReporter.class);

	private RunNotifier notifier;
	private final Description rootDescription;
	private final ArrayList<Description> storyDescriptions;

	private Description currentStoryDescription;
	private String currentStoryTitle;
	private String currentScenarioTitle;
	int testCounter = 0;
	private final int totalTests;

	public Set<Description> failedSteps = new HashSet<Description>();

	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();

	public JUnitStoryReporter(RunNotifier notifier, int totalTests,
			Description rootDescription) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		this.storyDescriptions = rootDescription.getChildren();
	}

	@Override
	public void beforeStory(Story story, boolean isGivenStory) {
		currentStoryTitle = story.getName();
		logger.info("Before Story: {} {}", currentStoryTitle, isGivenStory ? "(given story)" : "");
		if (testCounter == 0) {
			notifier.fireTestRunStarted(rootDescription);
		}

        if (currentStoryDescription != null) {
            notifier.fireTestFinished(currentStoryDescription);
        }

        currentStoryDescription = null;
		for (Description storyDescription : storyDescriptions) {
			if (storyDescription.isTest()
					&& storyDescription.getDisplayName().equals(story.getName())) {
				notifier.fireTestStarted(storyDescription);
				currentStoryDescription = storyDescription;
			}
		}

		this.testCounter++;
	}

	@Override
	public void afterStory(boolean isGivenStory) {
		logger.info("After Story: {} {}", currentStoryTitle, isGivenStory ? "(given story)" : "");
		if (testCounter == totalTests) {
			Result result = new Result();
			notifier.fireTestRunFinished(result);
		}
	}

	@Override
	public void beforeScenario(String title) {
		currentScenarioTitle = title;
		logger.info("Before Scenario: {}", title);
	}

	@Override
	public void afterScenario() {
		logger.info("After Scenario: {}", currentScenarioTitle);
	}

	@Override
	public void beforeExamples(List<String> arg0, ExamplesTable arg1) {
		logger.trace("Before Examples: {}", arg0 != null ? arg0 : "n/a");
	}

	@Override
	public void example(Map<String, String> arg0) {
		logger.info("Example: {}", arg0);
	}

	@Override
	public void afterExamples() {
		logger.trace("{}", "afterExamples");
	}

	@Override
	public void beforeStep(String title) {
		logger.trace("Before Step: {}", title);
	}

	@Override
	public void failed(String step, Throwable e) {
		if (e instanceof UUIDExceptionWrapper) {
			e = ((UUIDExceptionWrapper) e).getCause();
		}
		logger.info("Step Failed: {} (cause: {})", step, e.getMessage());
        Description description = currentStoryDescription == null ? rootDescription : currentStoryDescription;
		notifier.fireTestFailure(new Failure(description, e));
	}

	@Override
	public void successful(String step) {
		logger.info("Step Succesful: {}", step);
	}

	@Override
	public void pending(String arg0) {
		logger.info("Pending: {}", arg0);
		if (pendingStepStrategy instanceof FailingUponPendingStep) {
		    Description description = currentStoryDescription == null ? rootDescription : currentStoryDescription;
			notifier.fireTestFailure(new Failure(description, new RuntimeException("Step is pending!")));
		}
	}

	@Override
	public void ignorable(String arg0) {
		logger.info("Ignorable: {}", arg0);
	}

	@Override
	public void notPerformed(String arg0) {
		logger.info("Not performed: {}", arg0);
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
