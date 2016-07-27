package de.codecentric.jbehave.junit.monitoring;

import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportGivenStoryEvents;
import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Story;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;

public class JUnitScenarioReporterTest {

	private static final String NAME_STEP = "step";
	private static final String NAME_SCENARIO = "scenario";
	private static final String NAME_STORY = "story";
	private static final String NAME_ROOT = "root";
	private static final int ONE_GIVEN = 1;
	private static final int ONE_STEP = 1;
	private static final int THREE_STEPS = 3;
	private static final int TWO_COMPOSITE_STEPS = 2;
	private static final int ONE_BEFORE_STORIES = 1;
	private static final int ONE_AFTER_STORIES = 1;

	@Mock
	RunNotifier notifier;
	private Description rootDescription;
	private Description storyDescription;
	private Description scenarioDescription;
	private Story story;
	private JUnitScenarioReporter reporter;
	private Keywords keywords;
	private ReporterVerifier reporterVerifier;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rootDescription = Description.createTestDescription(this.getClass(), NAME_ROOT);
		storyDescription = Description.createTestDescription(this.getClass(), NAME_STORY);
		rootDescription.addChild(storyDescription);
		scenarioDescription = Description.createTestDescription(this.getClass(), NAME_SCENARIO);
		storyDescription.addChild(scenarioDescription);

		story = new Story();
		story.namedAs("story(" + this.getClass().getName() + ")");
		keywords = new Keywords();
		reporterVerifier = new ReporterVerifier(notifier, storyDescription, scenarioDescription);
	}

	@Test
	public void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters()
			throws Exception {

		Description child1 = addChildToScenario(scenarioDescription, "child");
		Description child2 = addChildToScenario(scenarioDescription, "child.");
		Description child3 = addChildToScenario(scenarioDescription, "child..");

		reporter = new JUnitScenarioReporter(notifier, THREE_STEPS, rootDescription, keywords);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reportStepSuccess(reporter, NAME_STEP);
		reportStepSuccess(reporter, NAME_STEP);
		reportStepSuccess(reporter, NAME_STEP);
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		reporterVerifier.stepSuccess(child1);
		reporterVerifier.stepSuccess(child2);
		reporterVerifier.stepSuccess(child3);
	}

	@Test
	public void shouldHandleFailedSteps() throws Exception {

		Description child1 = addChildToScenario(scenarioDescription, "child");

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP, rootDescription, keywords);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reportStepFailure(reporter, NAME_STEP);
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		verify(notifier).fireTestStarted(child1);
		verify(notifier).fireTestFailure(Matchers.<Failure> anyObject());
		verify(notifier).fireTestFinished(child1);
	}

	@Test
	public void shouldHandleIgnorableSteps() throws Exception {
		Description comment = addChildToScenario(scenarioDescription, "!-- Comment");
		reporter = new JUnitScenarioReporter(notifier, ONE_STEP, rootDescription, keywords);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		reportIgnorable(reporter);
		verify(notifier).fireTestIgnored(comment);
		reportScenarioAndStoryFinish(reporter);
		verifyTestFinish();

	}

	@Test
	public void shouldNotifyAboutBeforeStories() {
		rootDescription = ReporterHelper.addBeforeStories(rootDescription, storyDescription);
		Description child = addChildToScenario(scenarioDescription, "child");

		reporter = new JUnitScenarioReporter(notifier, ONE_BEFORE_STORIES + ONE_STEP, rootDescription, keywords);

		Story beforeStoriesStory = new Story();
		beforeStoriesStory.namedAs("BeforeStories");

		reporter.beforeStory(beforeStoriesStory, false);
		reporterVerifier.testRunStarted();

		reporter.afterStory(false);
		reporterVerifier.stepSuccess(ReporterHelper.getBeforeStories(rootDescription));

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();

		reportStepSuccess(reporter, NAME_STEP);
		reporterVerifier.stepSuccess(child);
		reportScenarioAndStoryFinish(reporter);
		verifyTestFinish();
	}

	@Test
	public void failureInBeforeStoriesShouldCountOnce() {
		rootDescription = ReporterHelper.addBeforeStories(rootDescription, storyDescription);

		reporter = new JUnitScenarioReporter(notifier, ONE_BEFORE_STORIES + ONE_STEP, rootDescription, keywords);

		Story beforeStoriesStory = new Story();
		beforeStoriesStory.namedAs("BeforeStories");

		reporter.beforeStory(beforeStoriesStory, false);
		reporterVerifier.testRunStarted();

		reporter.failed("BeforeStories", new RuntimeException("..."));
		reporter.afterStory(false);
		verify(notifier).fireTestStarted(ReporterHelper.getBeforeStories(rootDescription));
		verify(notifier).fireTestFailure(Mockito.<Failure> anyObject());
		// Story, its scenario(s) and its step(s) should not start nor finish if 'before stories' failed.
	}

	@Test
	public void shouldNotifyAboutAfterStories() {
		Description child = addChildToScenario(scenarioDescription, "child");
		Description afterStories = Description.createTestDescription(Object.class, "AfterStories");
		rootDescription.addChild(afterStories);

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP + ONE_AFTER_STORIES, rootDescription, keywords);

		Story afterStoriesStory = new Story();
		afterStoriesStory.namedAs("AfterStories");

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();

		reportStepSuccess(reporter, NAME_STEP);
		reporterVerifier.stepSuccess(child);
		reportScenarioAndStoryFinish(reporter);
		reporterVerifier.scenarioFinished();
		reporterVerifier.storyFinished();

		reporter.beforeStory(afterStoriesStory, false);
		reporter.afterStory(false);
		reporterVerifier.stepSuccess(afterStories);

		reporterVerifier.testRunFinished();
	}

	@Test
	public void shouldNotifyGivenStory() {

		Description givenStoryDescription = Description
				.createSuiteDescription("aGivenStory");
		scenarioDescription.addChild(givenStoryDescription);
		Description child = addChildToScenario(scenarioDescription, "child");

		reporter = new JUnitScenarioReporter(notifier, ONE_GIVEN + ONE_STEP, rootDescription, keywords);

		Story givenStory = new Story();
		givenStory.namedAs("aGivenStory");

		reporter.beforeStory(story, false);
		reporter.beforeScenario(scenarioDescription.getDisplayName());
		reportGivenStoryEvents(reporter);
		reportStepSuccess(reporter, NAME_STEP);
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		reporterVerifier.stepSuccess(givenStoryDescription);
		reporterVerifier.stepSuccess(child);
		verifyTestFinish();
	}

	@Test
	public void shouldNotifyCompositeSteps() {
		// one story, one scenario, one step, two composite steps
		Description child = addChildToScenario(scenarioDescription, "child");
		Description comp1 = Description.createTestDescription(this.getClass(), "comp1");
		child.addChild(comp1);
		Description comp2 = Description.createTestDescription(this.getClass(), "comp2");
		child.addChild(comp2);

		reporter = new JUnitScenarioReporter(notifier, TWO_COMPOSITE_STEPS, rootDescription, keywords);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporter.beforeStep("child");
		reporter.successful("child");
		reporter.beforeStep("comp1");
		reporter.successful("comp1");
		reporter.beforeStep("comp2");
		reporter.successful("comp2");
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		verify(notifier).fireTestStarted(child);
		reporterVerifier.stepSuccess(comp1);
		reporterVerifier.stepSuccess(comp2);
		verify(notifier).fireTestFinished(child);
		verifyTestFinish();
	}

	@Test
	public void shouldPrepareExampleStepsBeforeScenario() {
		// one story, one scenario, one example, one step,
		Description example = addChildToScenario(scenarioDescription, keywords.examplesTableRow() + " " + "row");
		Description step = Description.createTestDescription(this.getClass(), "Step");
		example.addChild(step);
		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);
		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporter.example(null);
		reportStepSuccess(reporter, NAME_STEP);
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		reporterVerifier.stepSuccess(step);
		verifyTestFinish();
	}

	@Test
	public void shouldHandleExampleStepsInCombinationWithGivenStories() {
		// one story, one scenario, one given story, one example, one step
		Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
		scenarioDescription.addChild(givenStoryDescription);
		// one story, one scenario, one example, one step,
		Description example = addChildToScenario(scenarioDescription, keywords.examplesTableRow() + " " + "row");
		Description step = Description.createTestDescription(this.getClass(), "Step");
		example.addChild(step);

		reporter = new JUnitScenarioReporter(notifier, ONE_GIVEN + ONE_STEP,
				rootDescription, keywords);

		reporter.beforeStory(story, false);
		reporter.beforeScenario(NAME_SCENARIO);
		reportGivenStoryEvents(reporter);
		reporter.example(null);
		reportStepSuccess(reporter, NAME_STEP);
		reportScenarioAndStoryFinish(reporter);

		reporterVerifier.testRunStarted();
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		reporterVerifier.stepSuccess(givenStoryDescription);
		reporterVerifier.stepSuccess(step);
		verifyTestFinish();
	}

	@Test
	public void shouldFailForPendingStepsAtBothStepAndScenarioLevelsIfConfigurationSaysSo() {
		Description child = addChildToScenario(scenarioDescription, "child");
		reporter = new JUnitScenarioReporter(notifier, ONE_STEP, rootDescription, keywords);

		PendingStepStrategy strategy = new FailingUponPendingStep();
		reporter.usePendingStepStrategy(strategy);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporter.pending("child");
		reporter.failed("child", new UUIDExceptionWrapper(new Exception("FAIL")));
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		verify(notifier).fireTestStarted(child);
		verify(notifier, times(2)).fireTestFailure(Mockito.<Failure> anyObject());
		verify(notifier, times(2)).fireTestFinished(child);
	}

	@Test
	public void shouldIgnorePendingStepsIfConfigurationSaysSo() {
		Description child = addChildToScenario(scenarioDescription, "child");

		reporter = new JUnitScenarioReporter(notifier, 3, rootDescription, keywords);

		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporter.pending("child");
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
		verify(notifier, VerificationModeFactory.times(0)).fireTestStarted(child);
		verify(notifier).fireTestIgnored(child);
	}

	@Test
	public void shouldHandleFailuresInBeforeStories() {
		reporter = new JUnitScenarioReporter(notifier, 1, rootDescription, keywords);

		reporter.beforeStory(story, false);
		reporter.failed(NAME_STORY, new UUIDExceptionWrapper("Error Message", new RuntimeException("Cause")));
		ArgumentCaptor<Failure> argument = ArgumentCaptor.forClass(Failure.class);
		verify(notifier).fireTestFailure(argument.capture());
		assertThat(argument.getValue().getDescription(), is(storyDescription));
	}

	private void reportScenarioAndStoryFinish(JUnitScenarioReporter reporter) {
		reporter.afterScenario();
		// test should not be finished until we send the final event
		verify(notifier, VerificationModeFactory.times(0)).fireTestRunFinished(
				Mockito.<Result> anyObject());
		reporter.afterStory(false);
	}

	private void verifyTestFinish() {
		reporterVerifier.scenarioFinished();
		reporterVerifier.storyFinished();
		reporterVerifier.testRunFinished();
	}

	private void verifyTestStart() {
		reporterVerifier.testRunStarted();
		reporterVerifier.storyStarted();
		reporterVerifier.scenarioStarted();
	}
}
