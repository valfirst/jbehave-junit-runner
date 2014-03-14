package de.codecentric.jbehave.junit.monitoring;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

import de.codecentric.jbehave.junit.monitoring.step.ExampleSteps;

@RunWith(JUnitReportingRunner.class)
public class ExampleScenarioJUnitStories extends JUnitStories {

	public ExampleScenarioJUnitStories() {
		JUnitReportingRunner.recommendedControls(configuredEmbedder());
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps());
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration();
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList(
				"de/codecentric/jbehave/junit/monitoring/Multiplication.story",
				"de/codecentric/jbehave/junit/monitoring/Empty.story");
	}

}
