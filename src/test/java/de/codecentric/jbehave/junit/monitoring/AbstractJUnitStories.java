package de.codecentric.jbehave.junit.monitoring;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import de.codecentric.jbehave.junit.monitoring.step.ExampleSteps;

/**
 * Abstract {@link JUnitStories} implementation.
 * 
 * @author Michal Bocek
 * @since 4/8/2016
 */
public class AbstractJUnitStories extends JUnitStories {

	public AbstractJUnitStories() {
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
		return Arrays.asList("de/codecentric/jbehave/junit/monitoring/Multiplication.story");
	}

}
