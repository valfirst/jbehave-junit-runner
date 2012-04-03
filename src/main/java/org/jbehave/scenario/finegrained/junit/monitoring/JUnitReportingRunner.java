package org.jbehave.scenario.finegrained.junit.monitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class JUnitReportingRunner extends Runner {
    private List<Description> storyDescriptions;
    private Embedder configuredEmbedder;
    private List<String> storyPaths;
    private JUnitStories junitStories;
    private Configuration configuration;
	private int testCases;
	private Description rootDescription;

    @SuppressWarnings("unchecked")
    public JUnitReportingRunner(Class<? extends JUnitStories> testClass) throws Throwable {

        junitStories = testClass.newInstance();
        configuredEmbedder = junitStories.configuredEmbedder();
        configuration = configuredEmbedder.configuration();

        Method method = testClass.getDeclaredMethod("storyPaths", (Class[]) null);
        method.setAccessible(true);
        storyPaths = ((List<String>) method.invoke(junitStories, (Object[]) null));

        storyDescriptions = buildDescriptionFromStories();

    }

	@Override
    public Description getDescription() {
        rootDescription = Description.createSuiteDescription(junitStories.getClass());
        rootDescription.getChildren().addAll(storyDescriptions);
        return rootDescription;
    }

    @Override
    public int testCount() {
        return testCases;
    }

    @Override
    public void run(RunNotifier notifier) {

        JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier, testCases, rootDescription);

        StoryReporterBuilder reporterBuilder = new StoryReporterBuilder().withReporters(reporter);
        Configuration junitReportingConfiguration = junitStories.configuration().useStoryReporterBuilder(reporterBuilder);
        configuredEmbedder.useConfiguration(junitReportingConfiguration);

        try {
            configuredEmbedder.runStoriesAsPaths(storyPaths);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            configuredEmbedder.generateCrossReference();
        }
    }

    private List<Description> buildDescriptionFromStories() {
    	
        JUnitDescriptionGenerator gen = new JUnitDescriptionGenerator(junitStories.stepsFactory().createCandidateSteps());
        StoryRunner storyRunner = new StoryRunner();
        List<Description> storyDescriptions = new ArrayList<Description>();

        storyDescriptions.add(Description.createTestDescription(Object.class, "BeforeStories"));
        testCases++;
        for (String storyPath : storyPaths) {
            Story parseStory = storyRunner.storyOfPath(configuration, storyPath);
            Description descr = gen.createDescriptionFrom(parseStory);
            storyDescriptions.add(descr);
        }
        storyDescriptions.add(Description.createTestDescription(Object.class, "AfterStories"));
        testCases++;
        testCases += gen.getTestCases();
        return storyDescriptions;
    }
}