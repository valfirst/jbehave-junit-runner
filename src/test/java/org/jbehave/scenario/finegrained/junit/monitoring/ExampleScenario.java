package org.jbehave.scenario.finegrained.junit.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.scenario.finegrained.junit.monitoring.step.ExampleSteps;
import org.junit.runner.RunWith;

@RunWith(JUnitReportingRunner.class)
public class ExampleScenario extends JUnitStories {

	public ExampleScenario() {
		configuredEmbedder().embedderControls().useThreads(1);

	}
	
	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps());
	}

	@Override
	public Configuration configuration() {
		// add custom coverters
		return new MostUsefulConfiguration();
	}

	@Override
	protected List<String> storyPaths() {
		List<String> stories = new ArrayList<String>();
		stories.add("org/jbehave/scenario/finegrained/junit/monitoring/Multiplication.story");
		return stories;
	}

}
