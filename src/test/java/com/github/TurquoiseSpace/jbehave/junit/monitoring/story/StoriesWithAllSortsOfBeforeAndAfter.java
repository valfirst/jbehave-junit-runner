package com.github.TurquoiseSpace.jbehave.junit.monitoring.story;

import java.util.Collections;
import java.util.List;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.step.ExampleSteps;
import com.github.TurquoiseSpace.jbehave.junit.monitoring.step.InitSteps;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.RunWith;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.JUnitReportingRunner;

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

	private final Configuration configuration;

	public StoriesWithAllSortsOfBeforeAndAfter() {
		StepMonitor stepMonitor = new DelegatingStepMonitor(
				new NullStepMonitor());
		configuration = new MostUsefulConfiguration()
				.useStepMonitor(stepMonitor)
				.usePendingStepStrategy(new FailingUponPendingStep())
				.useStoryReporterBuilder(
						new StoryReporterBuilder().withDefaultFormats()
								.withFailureTrace(true)
								.withFormats(Format.XML, Format.HTML))
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
	public List<String> storyPaths() {
		return Collections.singletonList(
				"com/github/TurquoiseSpace/jbehave/junit/monitoring/story/MultiplicationWithExamplesAndGiven.story");
	}
}
