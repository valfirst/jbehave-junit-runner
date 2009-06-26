package org.jbehave.scenario.finegrained.junit.monitoring;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.definition.StoryDefinition;
import org.jbehave.scenario.steps.Steps;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class JUnitReportingRunner extends Runner {
    private JUnitScenario testInstance;
    private Description storyDescription;
    private JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator();
    private ReflectionHelper reflectionHelper;

    public JUnitReportingRunner(Class<? extends JUnitScenario> testClass) {
	reflectionHelper = new ReflectionHelper(this.getClass(), testClass);
	StoryDefinition story = reflectionHelper.reflectMeAConfiguration().forDefiningScenarios().loadScenarioDefinitionsFor(testClass);
	Steps candidateSteps = reflectionHelper.reflectMeCandidateSteps();
	storyDescription = descriptionGenerator.createDescriptionFrom(story, candidateSteps, testClass);
    }

    @Override
    public Description getDescription() {
	return storyDescription;
    }

    @Override
    public void run(RunNotifier notifier) {
	JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier, storyDescription);
	testInstance = reflectionHelper.reflectMeATestInstance(reporter);

	try {
	    testInstance.runScenario();
	} catch (Throwable e) {
	    throw new RuntimeException(e);
	}
    }

}