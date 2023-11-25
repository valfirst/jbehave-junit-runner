package com.github.TurquoiseSpace.jbehave.junit.monitoring.story;

import java.util.Collections;
import java.util.List;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.step.ExampleSteps;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
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
public class MathStories extends JUnitStories {

	public MathStories() {
		JUnitReportingRunner.recommendedControls(configuredEmbedder());
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration()
				.useStoryReporterBuilder(new StoryReporterBuilder()
						.withDefaultFormats());
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps());
	}

	@Override
	protected List<String> storyPaths() {
		return Collections.singletonList("com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Multiplication.story");
	}

}
