package de.codecentric.jbehave.junit.monitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class JUnitReportingRunner extends Runner {
	private List<Description> storyDescriptions;
	private Embedder configuredEmbedder;
	private List<String> storyPaths;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	List<CandidateSteps> candidateSteps;
	private ConfigurableEmbedder embedder;

	@SuppressWarnings("unchecked")
	public JUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass)
			throws Throwable {
		embedder = testClass.newInstance();
		if (embedder instanceof JUnitStories) {
			JUnitStories junitStories = (JUnitStories) embedder;
			configuredEmbedder = junitStories.configuredEmbedder();
			Method method;
			try {
				method = testClass.getDeclaredMethod("storyPaths",
						(Class[]) null);
			} catch (NoSuchMethodException e) {
				method = testClass.getMethod("storyPaths", (Class[]) null);
			}
			method.setAccessible(true);
			storyPaths = ((List<String>) method.invoke(junitStories,
					(Object[]) null));

		} else if (embedder instanceof JUnitStory) {
			JUnitStory junitStory = (JUnitStory) embedder;
			configuredEmbedder = junitStory.configuredEmbedder();
			StoryPathResolver resolver = configuredEmbedder.configuration()
					.storyPathResolver();
			storyPaths = Arrays.asList(resolver.resolve(junitStory.getClass()));
		}

		configuration = configuredEmbedder.configuration();
		candidateSteps = embedder.stepsFactory().createCandidateSteps();

		storyDescriptions = buildDescriptionFromStories();
	}

	@Override
	public Description getDescription() {
		rootDescription = Description.createSuiteDescription(embedder
				.getClass());
		rootDescription.getChildren().addAll(storyDescriptions);
		return rootDescription;
	}

	@Override
	public int testCount() {
		return numberOfTestCases;
	}

	@Override
	public void run(RunNotifier notifier) {

		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				numberOfTestCases, rootDescription);
		reporter.usePendingStepStrateg(configuration.pendingStepStrategy());

		configuration.storyReporterBuilder().withFormats(
				new StoryReporterBuilder.ProvidedFormat(reporter));

		try {
			configuredEmbedder.runStoriesAsPaths(storyPaths);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			configuredEmbedder.generateCrossReference();
		}
	}

	private List<Description> buildDescriptionFromStories() {
		JUnitDescriptionGenerator gen = new JUnitDescriptionGenerator(
				candidateSteps, configuration);
		StoryRunner storyRunner = new StoryRunner();
		List<Description> storyDescriptions = new ArrayList<Description>();

		storyDescriptions.add(Description.createTestDescription(Object.class,
				"BeforeStories"));
		numberOfTestCases++;
		for (String storyPath : storyPaths) {
			Story parseStory = storyRunner
					.storyOfPath(configuration, storyPath);
			Description descr = gen.createDescriptionFrom(parseStory);
			storyDescriptions.add(descr);
		}
		storyDescriptions.add(Description.createTestDescription(Object.class,
				"AfterStories"));
		numberOfTestCases++;
		numberOfTestCases += gen.getTestCases();
		return storyDescriptions;
	}
}