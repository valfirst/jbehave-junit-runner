package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
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

		List<String> storyPaths = new StoryPathsExtractor(configurableEmbedder).getStoryPaths();

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
	 * Returns a {@link Statement}: Call {@link #runChild(org.junit.runners.model.FrameworkMethod, RunNotifier)}
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

				configurableEmbedder.run();
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
		List<CandidateSteps> candidateSteps = getCandidateSteps();
		JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(candidateSteps, configuration);
		List<Description> storyDescriptions = new ArrayList<>();

		addSuite(storyDescriptions, "BeforeStories");
		PerformableTree performableTree = createPerformableTree(candidateSteps, storyPaths);
		storyDescriptions.addAll(descriptionGenerator.createDescriptionFrom(performableTree));
		addSuite(storyDescriptions, "AfterStories");

		numberOfTestCases += descriptionGenerator.getTestCases();

		return storyDescriptions;
	}

	private PerformableTree createPerformableTree(List<CandidateSteps> candidateSteps, List<String> storyPaths) {
		BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
		PerformableTree performableTree = configuredEmbedder.performableTree();
		RunContext context = performableTree.newRunContext(configuration, candidateSteps,
				configuredEmbedder.embedderMonitor(), configuredEmbedder.metaFilter(), failures);
		performableTree.addStories(context, configuredEmbedder.storyManager().storiesOfPaths(storyPaths));
		return performableTree;
	}

	private void addSuite(List<Description> storyDescriptions, String name) {
		storyDescriptions.add(Description.createTestDescription(Object.class,
				name));
		numberOfTestCases++;
	}
}
