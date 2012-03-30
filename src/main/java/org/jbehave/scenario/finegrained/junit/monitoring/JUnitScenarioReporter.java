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
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitScenarioReporter implements StoryReporter {

    private RunNotifier notifier;
    private Description currentScenario;
    private Description currentStep;
    private Iterator<Description> scenarioDescriptions;
    private final Description[] storyDescriptions;
    List<Description> finishedDescriptions = new ArrayList<Description>();
    private JUnitDescriptionGenerator gen;

    private Description currentStoryDescription;
    private Iterator<Description> stepDescriptions;
    private Iterator<Description> exampleDescriptions;
    private Description nextExample;

    public JUnitScenarioReporter(RunNotifier notifier, Description... storyDescriptions) {
        gen = new JUnitDescriptionGenerator();
        this.notifier = notifier;
        this.storyDescriptions = storyDescriptions;
        // this.scenarioDescriptions =
        // storyDescriptions.getChildren().iterator();
        // this.currentScenario = this.scenarioDescriptions.next();
    }

    public void beforeStory(Story story, boolean arg1) {
        System.out.println("JUnitScenarioReporter.beforeStory()");
        for (Description storyDescription : storyDescriptions) {
            if (storyDescription.getDisplayName().equals(story.getName())) {
                currentStoryDescription = storyDescription;
                notifier.fireTestStarted(storyDescription);

                scenarioDescriptions = storyDescription.getChildren().iterator();
                if (scenarioDescriptions.hasNext()) {
                    currentScenario = scenarioDescriptions.next();
                    finishedDescriptions.clear();
                }
            }
        }

    }

    public void afterScenario() {
        notifier.fireTestFinished(currentScenario);
        if (scenarioDescriptions.hasNext()) {
            currentScenario = scenarioDescriptions.next();
            finishedDescriptions.clear();
        }
    }

    public void afterStory() {
        // notifier.fireTestFinished(storyDescriptions);
    }

    public void beforeScenario(String title) {
        System.out.println("JUnitScenarioReporter.beforeScenario()");
        notifier.fireTestStarted(currentScenario);

        stepDescriptions = currentScenario.getChildren().iterator();
        exampleDescriptions = stepDescriptions;
        if (stepDescriptions.hasNext()) {
            currentStep = stepDescriptions.next();
            nextExample = currentStep;
        }
    }

    public void beforeStep(String arg0) {
        System.out.println("JUnitScenarioReporter.beforeStep()");
        notifier.fireTestStarted(currentStep);
    }

    public void failed(String step, Throwable e) {
        if (e instanceof UUIDExceptionWrapper) {
            e = ((UUIDExceptionWrapper) e).getCause();
        }

        notifier.fireTestFailure(new Failure(currentStep, e));
        finishedDescriptions.add(currentStep);
    }

    public void successful(String step) {
        notifier.fireTestFinished(currentStep);
        finishedDescriptions.add(currentStep);

        if (stepDescriptions.hasNext()) {
            currentStep = stepDescriptions.next();
        }
    }

    public void afterStory(boolean arg0) {
        System.out.println("JBehave2JunitReporter.afterStory():: " + arg0);
    }

    public void afterExamples() {
        System.out.println("JBehave2JunitReporter.afterExamples()");

    }

    public void beforeExamples(List<String> arg0, ExamplesTable arg1) {
        System.out.println("JBehave2JunitReporter.beforeExamples()");
        // exampleDescriptions = currentScenario.getChildren().iterator();

    }

    public void dryRun() {

    }

    public void example(Map<String, String> arg0) {
        System.out.println("JBehave2JunitReporter.example()");

        stepDescriptions = nextExample.getChildren().iterator();
        if (stepDescriptions.hasNext()) {
            currentStep = stepDescriptions.next();
        }

        if (exampleDescriptions.hasNext()) {
            nextExample = exampleDescriptions.next();
        }

    }

    public void failedOutcomes(String arg0, OutcomesTable arg1) {
        System.out.println("JBehave2JunitReporter.failedOutcomes()");
    }

    public void givenStories(GivenStories arg0) {
        System.out.println("JBehave2JunitReporter.givenStories()");
    }

    public void givenStories(List<String> arg0) {
        System.out.println("JBehave2JunitReporter.givenStories()");
    }

    public void ignorable(String arg0) {
        System.out.println("JBehave2JunitReporter.ignorable()");
    }

    public void narrative(Narrative arg0) {
        // TODO Auto-generated method stub
        System.out.println("JBehave2JunitReporter.narrative()");
    }

    public void notPerformed(String arg0) {
        System.out.println("JBehave2JunitReporter.notPerformed()");
    }

    public void pending(String arg0) {
        System.out.println("JBehave2JunitReporter.pending()");
    }

    public void pendingMethods(List<String> arg0) {
        System.out.println("JBehave2JunitReporter.pendingMethods()");
    }

    public void restarted(String arg0, Throwable arg1) {
        System.out.println("JBehave2JunitReporter.restarted()");
    }

    public void scenarioMeta(Meta arg0) {
        System.out.println("JBehave2JunitReporter.scenarioMeta()");
    }

    public void scenarioNotAllowed(Scenario arg0, String arg1) {
        System.out.println("JBehave2JunitReporter.scenarioNotAllowed()");
    }

    public void storyCancelled(Story arg0, StoryDuration arg1) {
        System.out.println("JBehave2JunitReporter.storyCancelled()");
    }

    public void storyNotAllowed(Story arg0, String arg1) {
        System.out.println("JBehave2JunitReporter.storyNotAllowed()");
    }

}
