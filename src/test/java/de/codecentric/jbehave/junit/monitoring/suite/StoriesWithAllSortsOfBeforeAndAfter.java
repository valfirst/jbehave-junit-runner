package de.codecentric.jbehave.junit.monitoring.suite;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.RunWith;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import de.codecentric.jbehave.junit.monitoring.Logger;
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

	private Configuration configuration;

	public StoriesWithAllSortsOfBeforeAndAfter() {
		System.setProperty(Logger.PROP_JJM_LOGLEVEL, "debug");
		CrossReference crossReference = new CrossReference("dummy")
				.withJsonOnly().withOutputAfterEachStory(true)
				.excludingStoriesWithNoExecutedScenarios(true);
		StepMonitor stepMonitor = new DelegatingStepMonitor(
				crossReference.getStepMonitor());
		configuration = new MostUsefulConfiguration()
				.useStepMonitor(stepMonitor)
				.usePendingStepStrategy(new FailingUponPendingStep())
				.useStoryReporterBuilder(
						new StoryReporterBuilder().withDefaultFormats()
								.withFailureTrace(true)
								.withFormats(Format.XML, Format.HTML)
								.withCrossReference(crossReference))
				.useParameterControls(new ParameterControls("<", ">", true));
		JUnitReportingRunner.recommendedControls(configuredEmbedder());
	}

	@Override
	public Configuration configuration() {
		// when working with CrossReferences, you have to return the same
		// configuration INSTANCE => generate it once (in the constructor)
		// and reuse it here.
		return configuration;
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps(),
				new InitSteps());
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays
				.asList("de/codecentric/jbehave/junit/monitoring/MultiplicationWithExamplesAndGiven.story");
	}

}
