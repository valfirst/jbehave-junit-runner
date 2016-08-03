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
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitReportingRunner extends BlockJUnit4ClassRunner {

	private static Logger logger = LoggerFactory.getLogger(JUnitReportingRunner.class);

	private static final String REPORT_STORY_LEVEL = "report.storyLevel";
	private static final String AFTER_STORIES = "AfterStories";
	private static final String BEFORE_STORIES = "BeforeStories";

	private List<Description> storyDescriptions;
	private Embedder configuredEmbedder;
	private List<String> storyPaths;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	private List<CandidateSteps> candidateSteps;
	private ConfigurableEmbedder configurableEmbedder;

	public JUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass)
			throws Throwable {
		super(testClass);
		configurableEmbedder = testClass.newInstance();

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
				ExtendedStoryReporter junitReporter;
				if (getReportLevel() == JUnitReportLevel.STORY) {
					junitReporter = new JUnitStoryReporter(
							notifier, numberOfTestCases, rootDescription);
				} else {
					junitReporter = new JUnitScenarioReporter(
							notifier, numberOfTestCases, rootDescription, configuration.keywords());
				}
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
		getCandidateSteps();
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
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
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
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
		Method method = makeStoryPathsMethodPublic(testClass);
		storyPaths = ((List<String>) method.invoke(
				(JUnitStories) configurableEmbedder, (Object[]) null));
	}

	private Method makeStoryPathsMethodPublic(
			Class<? extends ConfigurableEmbedder> testClass)
			throws NoSuchMethodException {
		Method method;
		try {
			method = testClass.getDeclaredMethod("storyPaths", (Class[]) null);
		} catch (NoSuchMethodException e) {
			method = testClass.getMethod("storyPaths", (Class[]) null);
		}
		method.setAccessible(true);
		return method;
	}

	private void getCandidateSteps() {
		// candidateSteps = configurableEmbedder.configuredEmbedder()
		// .stepsFactory().createCandidateSteps();
		InjectableStepsFactory stepsFactory = configurableEmbedder
				.stepsFactory();
		if (stepsFactory != null) {
			candidateSteps = stepsFactory.createCandidateSteps();
		} else {
			Embedder embedder = configurableEmbedder.configuredEmbedder();
			candidateSteps = embedder.candidateSteps();
			if (candidateSteps == null || candidateSteps.isEmpty()) {
				candidateSteps = embedder.stepsFactory().createCandidateSteps();
			}
		}
	}

	private void initRootDescription() {
		rootDescription = Description
				.createSuiteDescription(configurableEmbedder.getClass());
		for (Description storyDescription : storyDescriptions) {
			rootDescription.addChild(storyDescription);
		}
	}

	private void addToStoryReporterFormats(StoryReporter junitReporter) {
		StoryReporterBuilder storyReporterBuilder = configuration
				.storyReporterBuilder();
		StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(
				junitReporter);
		storyReporterBuilder.withFormats(junitReportFormat);
	}

	private List<Description> buildDescriptionFromStories() {
		JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(
				candidateSteps, configuration);
		StoryRunner storyRunner = new StoryRunner();
		List<Description> storyDescriptions = new ArrayList<Description>();

		addSuite(storyDescriptions, BEFORE_STORIES);
		addStories(storyDescriptions, storyRunner, descriptionGenerator);
		addSuite(storyDescriptions, AFTER_STORIES);

		numberOfTestCases += descriptionGenerator.getTestCases();

		return storyDescriptions;
	}

	private void addStories(List<Description> storyDescriptions,
			StoryRunner storyRunner, JUnitDescriptionGenerator gen) {
		for (String storyPath : storyPaths) {
			Story parseStory = storyRunner.storyOfPath(configuration, storyPath);
			Description descr = gen.createDescriptionFrom(parseStory, getReportLevel());
			storyDescriptions.add(descr);
		}
	}

	private void addSuite(List<Description> storyDescriptions, String name) {
		if (getReportLevel() == JUnitReportLevel.STORY &&
				(name.equals(BEFORE_STORIES) || name.equals(AFTER_STORIES))) {
			logger.debug("For story level do not include: {}", name);
		} else {
			storyDescriptions.add(Description.createTestDescription(Story.class, name));
		}
        numberOfTestCases++;
	}

	private JUnitReportLevel getReportLevel() {
		JUnitReportLevel result;
		if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(REPORT_STORY_LEVEL))) {
			result = JUnitReportLevel.STORY;
		} else {
			result = JUnitReportLevel.STEP;
		}
		return result;
	}
}
