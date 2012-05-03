package org.jbehave.scenario.finegrained.junit.monitoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
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

public class JUnitScenarioReporter implements StoryReporter {
    Logger logger = LoggerFactory.getLogger(JUnitScenarioReporter.class);

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

    public JUnitScenarioReporter(RunNotifier notifier, int totalTests, Description rootDescription) {
        this.totalTests = totalTests;
        this.rootDescription = rootDescription;
        this.notifier = notifier;
        this.storyDescriptions = rootDescription.getChildren();
    }

    public void beforeStory(Story story, boolean isGivenStory) {
        logger.info("Before Story: {} {}", story.getName(), isGivenStory ? "(given story)" : "");
        if (testCounter == 0) {
            notifier.fireTestRunStarted(rootDescription);
        }
        for (Description storyDescription : storyDescriptions) {
        	if (storyDescription.isSuite() && storyDescription.getDisplayName().equals(story.getName())) {
                currentStoryDescription = storyDescription;
                notifier.fireTestStarted(storyDescription);

                scenarioDescriptions = storyDescription.getChildren().iterator();
                if (scenarioDescriptions.hasNext()) {
                    currentScenario = scenarioDescriptions.next();
                }
            } else if (storyDescription.isTest() && storyDescription.getMethodName().equals(story.getName())) {
                // Story BeforeStories or After Stories
                currentStoryDescription = storyDescription;
                notifier.fireTestStarted(currentStoryDescription);
            }
        }

    }

    public void afterScenario() {
        logger.info("After Scenario: {}", currentScenario.getDisplayName());
        notifier.fireTestFinished(currentScenario);
        if (scenarioDescriptions.hasNext()) {
            currentScenario = scenarioDescriptions.next();
            logger.debug("--> updating current scenario to {}", currentScenario.getDisplayName());
        }
    }

    public void afterStory(boolean isGivenStory) {
        logger.info("After Story: {} {}", currentStoryDescription.getDisplayName(), isGivenStory ? "(given story)" : "");
        notifier.fireTestFinished(currentStoryDescription);
        testCounter++;

        if (testCounter == totalTests) {
            Result result = new Result();
            notifier.fireTestRunFinished(result);
        }
    }

    public void beforeScenario(String title) {
        logger.info("Before Scenario: {}", title);
        notifier.fireTestStarted(currentScenario);

        stepDescriptions = currentScenario.getChildren().iterator();
        exampleDescriptions = stepDescriptions;
        if (stepDescriptions.hasNext()) {
            currentStep = stepDescriptions.next();
            nextExample = currentStep;
        }
    }

    public void beforeStep(String title) {
        logger.info("Before Step: {}", title);
        notifier.fireTestStarted(currentStep);
    }

    public void failed(String step, Throwable e) {
        if (e instanceof UUIDExceptionWrapper) {
            e = ((UUIDExceptionWrapper) e).getCause();
        }
        logger.info("Step Failed: {} (cause: {})", step, e.getMessage());
        notifier.fireTestFailure(new Failure(currentStep, e));
        testCounter++;
    }

    public void successful(String step) {
        logger.info("Step Succesful: {}", step);
        notifier.fireTestFinished(currentStep);

        if (stepDescriptions.hasNext()) {
            currentStep = stepDescriptions.next();
        } 
        testCounter++;
    }

    public void afterExamples() {
        logger.info("{}", "afterExamples");

    }

    public void beforeExamples(List<String> arg0, ExamplesTable arg1) {
        logger.info("Before Examples: {}", arg0 != null ? arg0 : "n/a");
    }

    public void dryRun() {
        logger.info("{}", "dryRun");
    }

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

    public void failedOutcomes(String arg0, OutcomesTable arg1) {
        logger.info("Failed outcomes: {}", arg0);
    }

    public void givenStories(GivenStories arg0) {
        logger.info("Given Stories: {}", arg0);
    }

    public void givenStories(List<String> arg0) {
        logger.info("Given Stories (List): {}", arg0);
    }

    public void ignorable(String arg0) {
        logger.info("Ignorable: {}", arg0);
    }

    public void narrative(Narrative arg0) {
        logger.info("Narrative: {}", arg0);
    }

    public void notPerformed(String arg0) {
        logger.info("Not performed: {}", arg0);
    }

    public void pending(String arg0) {
        logger.info("Pending: {}", arg0);
    }

    public void pendingMethods(List<String> arg0) {
        logger.info("Pending methods: {}", arg0);
    }

    public void restarted(String arg0, Throwable arg1) {
        logger.info("Restarted: {} ({})", arg0, arg1);
    }

    public void scenarioMeta(Meta arg0) {
        logger.info("Meta: {}", arg0);
    }

    public void scenarioNotAllowed(Scenario arg0, String arg1) {
        logger.info("Scenario not allowed: {} {}", arg0, arg1);
    }

    public void storyCancelled(Story arg0, StoryDuration arg1) {
        logger.info("Story cancelled: {} after {}", arg0, arg1);
        System.out.println("JBehave2JunitReporter.storyCancelled()");
    }

    public void storyNotAllowed(Story arg0, String arg1) {
        logger.info("Story not allowed: {}, {}", arg0, arg1);
    }

}
