package org.jbehave.scenario.finegrained.junit.monitoring.suite;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.scenario.finegrained.junit.monitoring.JUnitReportingRunner;
import org.jbehave.scenario.finegrained.junit.monitoring.step.ExampleSteps;
import org.jbehave.scenario.finegrained.junit.monitoring.step.InitSteps;
import org.junit.runner.RunWith;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the
 * {@link LoadFromClasspath} story loader is configured.
 * </p>
 */
@RunWith(JUnitReportingRunner.class)
public class StoriesWithAllSortsOfBeforeAndAfter extends JUnitStories {

	public StoriesWithAllSortsOfBeforeAndAfter() {
		configuredEmbedder()
		.embedderControls()
		.useThreads(1)
		.doIgnoreFailureInStories(true);
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration()
		.useParameterControls(new ParameterControls("<", ">", true));
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps(), new InitSteps());
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList(
				"org/jbehave/scenario/finegrained/junit/monitoring/MultiplicationWithExamplesAndGiven.story"
				);
	}

}
