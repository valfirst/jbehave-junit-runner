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

public class JUnitScenarioReporter implements StoryReporter {

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

	public JUnitScenarioReporter(RunNotifier notifier,
			int totalTests, Description rootDescription) {
		this.totalTests = totalTests;
		this.rootDescription = rootDescription;
		this.notifier = notifier;
		this.storyDescriptions = rootDescription.getChildren();
	}

	public void beforeStory(Story story, boolean arg1) {
		System.out.printf("JUnitScenarioReporter.beforeStory(%s, %s)\n",
				story.getName(), arg1);
		if (testCounter==0) {
			notifier.fireTestRunStarted(rootDescription);
		}
		for (Description storyDescription : storyDescriptions) {
			if (storyDescription.isSuite()
					&& storyDescription.getDisplayName()
							.equals(story.getName())) {
				currentStoryDescription = storyDescription;
				notifier.fireTestStarted(storyDescription);

				scenarioDescriptions = storyDescription.getChildren()
						.iterator();
				if (scenarioDescriptions.hasNext()) {
					currentScenario = scenarioDescriptions.next();
				}
			} else if (storyDescription.isTest() && storyDescription.getMethodName().equals(story.getName())){
				// Story BeforeStories or After Stories
				currentStoryDescription = storyDescription;
				notifier.fireTestStarted(currentStoryDescription);
			}
		}

	}

	public void afterScenario() {
		notifier.fireTestFinished(currentScenario);
		if (scenarioDescriptions.hasNext()) {
			currentScenario = scenarioDescriptions.next();
		}
	}

	public void afterStory(boolean arg0) {
		System.out.println("JBehave2JunitReporter.afterStory():: " + arg0);
		if (currentStoryDescription.isTest()) {
			notifier.fireTestFinished(currentStoryDescription);
			testCounter++;
		}
		
		if (testCounter == totalTests) {
			Result result = new Result();
			notifier.fireTestRunFinished(result);
		}
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
		System.out.println("JUnitScenarioReporter.failed()");
		if (e instanceof UUIDExceptionWrapper) {
			e = ((UUIDExceptionWrapper) e).getCause();
		}

		notifier.fireTestFailure(new Failure(currentStep, e));
		testCounter++;
	}

	public void successful(String step) {
		System.out.println("JUnitScenarioReporter.successful()");
		notifier.fireTestFinished(currentStep);

		if (stepDescriptions.hasNext()) {
			currentStep = stepDescriptions.next();
		}
		testCounter++;
	}


	public void afterExamples() {
		System.out.println("JBehave2JunitReporter.afterExamples()");

	}

	public void beforeExamples(List<String> arg0, ExamplesTable arg1) {
		System.out.println("JBehave2JunitReporter.beforeExamples()");
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
