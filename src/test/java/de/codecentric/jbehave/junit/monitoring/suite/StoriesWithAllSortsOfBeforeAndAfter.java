package de.codecentric.jbehave.junit.monitoring.suite;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.junit.runner.RunWith;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import de.codecentric.jbehave.junit.monitoring.step.ExampleSteps;
import de.codecentric.jbehave.junit.monitoring.step.InitSteps;

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
//		.doVerboseFailures(true)
		.useThreads(1);
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration()
		.usePendingStepStrategy( new FailingUponPendingStep())
		.useParameterControls(new ParameterControls("<", ">", true));
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps(), new InitSteps());
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList(
				"de/codecentric/jbehave/junit/monitoring/MultiplicationWithExamplesAndGiven.story"
				);
	}

}
