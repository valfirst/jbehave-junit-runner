package de.codecentric.jbehave.junit.monitoring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryPathResolver;
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
import org.junit.runners.model.Statement;

public class JUnitReportingRunner extends BlockJUnit4ClassRunner {
	private List<Description> storyDescriptions;
	private Embedder configuredEmbedder;
	private List<String> storyPaths;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	private List<CandidateSteps> candidateSteps;
	private ConfigurableEmbedder configurableEmbedder;

	@SuppressWarnings("unchecked")
	public JUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass)
			throws Throwable {
		super(testClass);
		configurableEmbedder = testClass.newInstance();
		configuredEmbedder = configurableEmbedder.configuredEmbedder();

		if (configurableEmbedder instanceof JUnitStories) {
			getStoryPathsFromJUnitStories(testClass);
		} else if (configurableEmbedder instanceof JUnitStory) {
			getStoryPathsFromJUnitStory();
		}

		configuration = configuredEmbedder.configuration();

		StepMonitor originalStepMonitor = createCandidateStepsWithNoMonitor();
		storyDescriptions = buildDescriptionFromStories();
		createCandidateStepsWith(originalStepMonitor);

		initRootDescription();
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
					configuredEmbedder.runStoriesAsPaths(storyPaths);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				} finally {
					configuredEmbedder.generateCrossReference();
				}
			}
		};
	}

	public static EmbedderControls recommandedControls(Embedder embedder) {
		return recommendedControls(embedder);
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

	private void createCandidateStepsWith(StepMonitor stepMonitor) {
		// reset step monitor and recreate candidate steps
		configuration.useStepMonitor(stepMonitor);
		candidateSteps = getCandidateSteps();
		for (CandidateSteps step : candidateSteps) {
			step.configuration().useStepMonitor(stepMonitor);
		}
	}

	private StepMonitor createCandidateStepsWithNoMonitor() {
		StepMonitor usedStepMonitor = configuration.stepMonitor();
		createCandidateStepsWith(new NullStepMonitor());
		return usedStepMonitor;
	}

	private void getStoryPathsFromJUnitStory() {
		StoryPathResolver resolver = configuredEmbedder.configuration()
				.storyPathResolver();
		storyPaths = Arrays.asList(resolver.resolve(configurableEmbedder
				.getClass()));
	}

	@SuppressWarnings("unchecked")
	private void getStoryPathsFromJUnitStories(
			Class<? extends ConfigurableEmbedder> testClass)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method method = makeStoryPathsMethodPublic(testClass);
		storyPaths = ((List<String>) method.invoke(
				(JUnitStories) configurableEmbedder, (Object[]) null));
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

	private void initRootDescription() {
		rootDescription = Description
				.createSuiteDescription(configurableEmbedder.getClass());
		for (Description storyDescription : storyDescriptions) {
			rootDescription.addChild(storyDescription);
		}
	}

	private void addToStoryReporterFormats(JUnitScenarioReporter junitReporter) {
		StoryReporterBuilder storyReporterBuilder = configuration
				.storyReporterBuilder();
		StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(
				junitReporter);
		storyReporterBuilder.withFormats(junitReportFormat);
	}

	private List<Description> buildDescriptionFromStories() {
		JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(
				candidateSteps, configuration);
		List<Description> storyDescriptions = new ArrayList<>();

		addSuite(storyDescriptions, "BeforeStories");
		storyDescriptions.addAll(descriptionGenerator.createDescriptionFrom(createPerformableTree()));
		addSuite(storyDescriptions, "AfterStories");

		numberOfTestCases += descriptionGenerator.getTestCases();

		return storyDescriptions;
	}

	private PerformableTree createPerformableTree() {
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
