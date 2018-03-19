package com.github.valfirst.jbehave.junit.monitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class JUnitReportingRunner extends BlockJUnit4ClassRunner {
	private Embedder configuredEmbedder;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	private ConfigurableEmbedder configurableEmbedder;

	public JUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass)
			throws InitializationError, ReflectiveOperationException {
		super(testClass);
		configurableEmbedder = testClass.newInstance();
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
		configuration = configuredEmbedder.configuration();

		List<String> storyPaths = getStoryPaths(testClass);

		StepMonitor originalStepMonitor = configuration.stepMonitor();
		configuration.useStepMonitor(new NullStepMonitor());
		List<Description> storyDescriptions = buildDescriptionFromStories(storyPaths);
		configuration.useStepMonitor(originalStepMonitor);

		rootDescription = Description.createSuiteDescription(testClass);
		for (Description storyDescription : storyDescriptions) {
			rootDescription.addChild(storyDescription);
		}
	}

	@Override
	public Description getDescription() {
		return rootDescription;
	}

	@Override
	public int testCount() {
		return numberOfTestCases;
	}

	/**
	 * Returns a {@link Statement}: Call {@link #runChild(Object, RunNotifier)}
	 * on each object returned by {@link #getChildren()} (subject to any imposed
	 * filter and sort)
	 */
	@Override
	protected Statement childrenInvoker(final RunNotifier notifier) {
		return new Statement() {
			@Override
			public void evaluate() {
				JUnitScenarioReporter junitReporter = new JUnitScenarioReporter(
				notifier, numberOfTestCases, rootDescription, configuration.keywords());
				// tell the reporter how to handle pending steps
				junitReporter.usePendingStepStrategy(configuration
						.pendingStepStrategy());

				addToStoryReporterFormats(junitReporter);

				try {
					configurableEmbedder.run();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static EmbedderControls recommendedControls(Embedder embedder) {
		return embedder.embedderControls()
		// don't throw an exception on generating reports for failing stories
				.doIgnoreFailureInView(true)
				// don't throw an exception when a story failed
				.doIgnoreFailureInStories(true)
				// .doVerboseFailures(true)
				.useThreads(1);
	}

	private List<String> getStoryPaths(Class<? extends ConfigurableEmbedder> testClass)
			throws ReflectiveOperationException {
		if (JUnitStories.class.isAssignableFrom(testClass)) {
			return getStoryPathsFromJUnitStories(testClass);
		} else if (JUnitStory.class.isAssignableFrom(testClass)) {
			return Collections.singletonList(configuration.storyPathResolver().resolve(testClass));
		} else {
			throw new IllegalArgumentException(
					"Only ConfigurableEmbedder of types JUnitStory and JUnitStories is supported");
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> getStoryPathsFromJUnitStories(
			Class<? extends ConfigurableEmbedder> testClass)
			throws ReflectiveOperationException {
		Method method = makeStoryPathsMethodPublic(testClass);
		return ((List<String>) method.invoke(configurableEmbedder, (Object[]) null));
	}

	@SuppressWarnings("unchecked")
    private static Method makeStoryPathsMethodPublic(Class<? extends ConfigurableEmbedder> clazz)
			throws NoSuchMethodException {
		try {
			Method method = clazz.getDeclaredMethod("storyPaths", (Class[]) null);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null && ConfigurableEmbedder.class.isAssignableFrom(superclass)) {
				return makeStoryPathsMethodPublic((Class<? extends ConfigurableEmbedder>) superclass);
			}
			throw e;
		}
	}

	private List<CandidateSteps> getCandidateSteps() {
		List<CandidateSteps> candidateSteps;
		InjectableStepsFactory stepsFactory = configurableEmbedder.stepsFactory();
		if (stepsFactory != null) {
			candidateSteps = stepsFactory.createCandidateSteps();
		} else {
			candidateSteps = configuredEmbedder.candidateSteps();
			if (candidateSteps == null || candidateSteps.isEmpty()) {
				candidateSteps = configuredEmbedder.stepsFactory().createCandidateSteps();
			}
		}
		return candidateSteps;
	}

	private void addToStoryReporterFormats(JUnitScenarioReporter junitReporter) {
		StoryReporterBuilder storyReporterBuilder = configuration
				.storyReporterBuilder();
		StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(
				junitReporter);
		storyReporterBuilder.withFormats(junitReportFormat);
	}

	private List<Description> buildDescriptionFromStories(List<String> storyPaths) {
		JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(
				getCandidateSteps(), configuration);
		List<Description> storyDescriptions = new ArrayList<>();

		addSuite(storyDescriptions, "BeforeStories");
		storyDescriptions.addAll(descriptionGenerator.createDescriptionFrom(createPerformableTree(storyPaths)));
		addSuite(storyDescriptions, "AfterStories");

		numberOfTestCases += descriptionGenerator.getTestCases();

		return storyDescriptions;
	}

	private PerformableTree createPerformableTree(List<String> storyPaths) {
		BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
		PerformableTree performableTree = new PerformableTree();
		RunContext context = performableTree.newRunContext(configuration, configuredEmbedder.stepsFactory(),
				configuredEmbedder.embedderMonitor(), configuredEmbedder.metaFilter(), failures);
		performableTree.addStories(context, storiesOf(performableTree, storyPaths));
		return performableTree;
	}

	private List<Story> storiesOf(PerformableTree performableTree, List<String> storyPaths) {
		List<Story> stories = new ArrayList<>();
		for (String storyPath : storyPaths) {
			stories.add(performableTree.storyOfPath(configuration, storyPath));
		}
		return stories;
	}

	private void addSuite(List<Description> storyDescriptions, String name) {
		storyDescriptions.add(Description.createTestDescription(Object.class,
				name));
		numberOfTestCases++;
	}
}
