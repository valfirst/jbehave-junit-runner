package com.github.TurquoiseSpace.jbehave.junit.monitoring.story;

import java.util.Arrays;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.JUnitReportingRunner;
import com.github.TurquoiseSpace.jbehave.junit.monitoring.step.ExampleSteps;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

@RunWith(JUnitReportingRunner.class)
public class FilteredOutScenariosNotStory extends JUnitStory {

	public FilteredOutScenariosNotStory() {
		EmbedderControls embedderControls = JUnitReportingRunner.recommendedControls(configuredEmbedder());
		embedderControls.doVerboseFailures(true);
		embedderControls.doIgnoreFailureInStories(false);
		configuredEmbedder().useMetaFilters(Arrays.asList("-first", "-second"));
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps());
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration();
	}
}
