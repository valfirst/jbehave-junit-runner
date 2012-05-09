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
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.scenario.finegrained.junit.monitoring.JUnitReportingRunner;
import org.jbehave.scenario.finegrained.junit.monitoring.step.ExampleSteps;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Scopes;

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
		configuredEmbedder().embedderControls().useThreads(1);
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration();
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new ExampleSteps());
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList("org/jbehave/scenario/finegrained/junit/monitoring/Multiplication.story");
	}

}
