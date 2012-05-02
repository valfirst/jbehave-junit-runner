package org.jbehave.scenario.finegrained.junit.monitoring.suite;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
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
public class AllAcceptanceTestStories extends JUnitStories {

	public AllAcceptanceTestStories() {
		configuredEmbedder().embedderControls().useThreads(1);
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration();
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new GuiceStepsFactory(configuration(), Guice.createInjector(new StepsModule()));
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList("org/jbehave/scenario/finegrained/junit/monitoring/Multiplication.story");
	}

	/**
	 * Defines the classes that contain the Steps of the Scenarios.
	 */
	public static class StepsModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(ExampleSteps.class).in(Scopes.SINGLETON);
		}

	}

}
