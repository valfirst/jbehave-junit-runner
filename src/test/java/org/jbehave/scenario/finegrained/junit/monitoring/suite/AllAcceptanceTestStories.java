package org.jbehave.scenario.finegrained.junit.monitoring.suite;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;

import java.text.SimpleDateFormat;
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
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
		configuredEmbedder().embedderControls()
				.doGenerateViewAfterStories(true)
				.doIgnoreFailureInStories(true).doIgnoreFailureInView(false)
				.useThreads(1).useStoryTimeoutInSecs(7200);
	}

	private Injector createInjector() {
		return Guice.createInjector(new StepsModule());
	}

	@Override
	public Configuration configuration() {
		Class<? extends Embeddable> embeddableClass = this.getClass();
		// Start from default ParameterConverters instance
		ParameterConverters parameterConverters = new ParameterConverters();
		// factory to allow parameter conversion and loading from external
		// resources (used by StoryParser too)
		ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(
				new LocalizedKeywords(),
				new LoadFromClasspath(embeddableClass), parameterConverters);
		// add custom coverters
		parameterConverters.addConverters(new DateConverter(
				new SimpleDateFormat("yyyy-MM-dd")),
				new ExamplesTableConverter(examplesTableFactory));
		return new MostUsefulConfiguration()
				.useStoryLoader(new LoadFromClasspath(embeddableClass))
				.useStoryParser(new RegexStoryParser(examplesTableFactory))
				.useStoryReporterBuilder(
						new StoryReporterBuilder()
								.withCodeLocation(
										CodeLocations
												.codeLocationFromClass(embeddableClass))
								.withDefaultFormats()
								.withFormats(CONSOLE))
				.useParameterConverters(parameterConverters);
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new GuiceStepsFactory(configuration(), createInjector());
	}

	private static void recastExceptionWhenFindingSteps(Exception e) {
		e.printStackTrace();
		throw new RuntimeException("Could not find Step classes", e);
	}

	@Override
	protected List<String> storyPaths() {
		String singleStoryName = System.getProperty("jbehave.story");
		String includePattern = "**/*.story";
		if (singleStoryName != null && singleStoryName.endsWith(".story")) {
			includePattern = "**/" + singleStoryName;
		}
		List<String> storyPaths = new StoryFinder().findPaths(
				codeLocationFromClass(this.getClass()), includePattern,
				"**/excluded*.story");
        return storyPaths;

	}

	/**
	 * Defines the classes that contain the Steps of the Scenarios.
	 */
	public static class StepsModule extends AbstractModule {

		@Override
		protected void configure() {
			List<String> stepNames = new StoryFinder().findPaths(
					codeLocationFromClass(this.getClass()),
					"**/step/**/*.class", "");
			stepNames.addAll(new StoryFinder().findPaths(
					codeLocationFromClass(this.getClass()),
					"**/library/**/*.class", ""));
			try {
				for (String stepName : stepNames) {

					String className = convertFilepathToClassname(stepName);
					if (!stepName.contains("$")) {
						bind(Class.forName(className)).in(Scopes.SINGLETON);
					}
				}
			} catch (ClassNotFoundException e) {
				recastExceptionWhenFindingSteps(e);
			}
		}

		private String convertFilepathToClassname(String stepName) {
			return stepName.replaceAll("/", ".").substring(0,
					stepName.length() - ".class".length());
		}
	}

}
