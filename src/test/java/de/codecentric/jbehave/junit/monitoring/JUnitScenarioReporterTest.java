package de.codecentric.jbehave.junit.monitoring;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Scenario;
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

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rootDescription = Description.createSuiteDescription(NAME_ROOT);
		storyDescription = Description.createSuiteDescription(NAME_STORY);
		rootDescription.addChild(storyDescription);
		scenarioDescription = Description.createTestDescription(
				this.getClass(), NAME_SCENARIO);
		storyDescription.addChild(scenarioDescription);

		story = new Story();
		story.namedAs(NAME_STORY);
		keywords = new Keywords();
	}

	@Test
	public void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters()
			throws Exception {

		Description child1 = addChildToScenario("child");
		Description child2 = addChildToScenario("child.");
		Description child3 = addChildToScenario("child..");

		reporter = new JUnitScenarioReporter(notifier, THREE_STEPS,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		reportStepSuccess(reporter);
		reportStepSuccess(reporter);
		reportStepSuccess(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		verifyStepSuccess(child1);
		verifyStepSuccess(child2);
		verifyStepSuccess(child3);
	}

	@Test
	public void shouldHandleFailedSteps() throws Exception {

		Description child1 = addChildToScenario("child");

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		reportStepFailure(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestStarted(child1);
		verify(notifier).fireTestFailure(Matchers.<Failure> anyObject());
		verify(notifier).fireTestFinished(child1);
	}

	@Test
	public void shouldHandleIgnorableSteps() throws Exception {
		Description comment = addChildToScenario("!-- Comment");
		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		reportIgnorable(reporter);
		verify(notifier).fireTestIgnored(comment);
		reportScenarioAndStoryFinish(reporter);
		verifyTestFinish();

	}

	@Test
	public void shouldNotifyAboutBeforeStories() {
		Description beforeStories = addBeforeStories();
		Description child = addChildToScenario("child");

		reporter = new JUnitScenarioReporter(notifier, ONE_BEFORE_STORIES
				+ ONE_STEP, rootDescription, keywords);

		Story beforeStoriesStory = new Story();
		beforeStoriesStory.namedAs("BeforeStories");

		reporter.beforeStory(beforeStoriesStory, false);
		verifyTestRunStarted();

		reporter.afterStory(false);
		verifyStepSuccess(beforeStories);

		reportStoryAndScenarioStart(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();

		reportStepSuccess(reporter);
		verifyStepSuccess(child);
		reportScenarioAndStoryFinish(reporter);
		verifyTestFinish();
	}

	@Test
	public void failureInBeforeStoriesShouldCountOnce() {
		Description beforeStories = addBeforeStories();

		reporter = new JUnitScenarioReporter(notifier, ONE_BEFORE_STORIES
				+ ONE_STEP, rootDescription, keywords);

		Story beforeStoriesStory = new Story();
		beforeStoriesStory.namedAs("BeforeStories");

		reporter.beforeStory(beforeStoriesStory, false);
		verifyTestRunStarted();

		reporter.failed("BeforeStories", new RuntimeException("..."));
		reporter.afterStory(false);
		verify(notifier).fireTestStarted(beforeStories);
		verify(notifier).fireTestFailure(Mockito.<Failure> anyObject());
		// Story, its scenario(s) and its step(s) should not start nor finish if 'before stories' failed.
	}

	@Test
	public void shouldNotifyAboutAfterStories() {
		Description child = addChildToScenario("child");
		Description afterStories = Description.createTestDescription(
				Object.class, "AfterStories");
		rootDescription.addChild(afterStories);

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP
				+ ONE_AFTER_STORIES, rootDescription, keywords);

		Story afterStoriesStory = new Story();
		afterStoriesStory.namedAs("AfterStories");

		reportStoryAndScenarioStart(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();

		reportStepSuccess(reporter);
		verifyStepSuccess(child);
		reportScenarioAndStoryFinish(reporter);
		verifyScenarioFinished();
		verifyStoryFinished();

		reporter.beforeStory(afterStoriesStory, false);
		reporter.afterStory(false);
		verifyStepSuccess(afterStories);

		verifyTestRunFinished();
	}

	@Test
	public void shouldNotifyGivenStory() {

		Description givenStoryDescription = Description
				.createSuiteDescription("aGivenStory");
		scenarioDescription.addChild(givenStoryDescription);
		Description child = addChildToScenario("child");

		reporter = new JUnitScenarioReporter(notifier, ONE_GIVEN + ONE_STEP,
				rootDescription, keywords);

		Story givenStory = new Story();
		givenStory.namedAs("aGivenStory");

		reporter.beforeStory(story, false);
		reporter.beforeScenario(scenarioDescription.getDisplayName());
		reportGivenStoryEvents();
		reportStepSuccess(reporter);
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		verifyStepSuccess(givenStoryDescription);
		verifyStepSuccess(child);
		verifyTestFinish();
	}

	@Test
	public void shouldNotifyCompositeSteps() {
		// one story, one scenario, one step, two composite steps
		Description child = addChildToScenario("child");
		Description comp1 = Description.createTestDescription(this.getClass(),
				"comp1");
		child.addChild(comp1);
		Description comp2 = Description.createTestDescription(this.getClass(),
				"comp2");
		child.addChild(comp2);

		reporter = new JUnitScenarioReporter(notifier, TWO_COMPOSITE_STEPS,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		reporter.beforeStep("child");
		reporter.successful("child");
		reporter.beforeStep("comp1");
		reporter.successful("comp1");
		reporter.beforeStep("comp2");
		reporter.successful("comp2");
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		verify(notifier).fireTestStarted(child);
		verifyStepSuccess(comp1);
		verifyStepSuccess(comp2);
		verify(notifier).fireTestFinished(child);
		verifyTestFinish();
	}

	@Test
	public void shouldPrepareExampleStepsBeforeScenario() {
		// one story, one scenario, one example, one step,
		Description example = addChildToScenario(keywords.examplesTableRow() + " "
				+ "row");
		Description step = Description.createTestDescription(this.getClass(),
				"Step");
		example.addChild(step);
		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);
		reportStoryAndScenarioStart(reporter);
		reporter.example(null);
		reportStepSuccess(reporter);
		reportScenarioAndStoryFinish(reporter);

		verifyTestStart();
		verifyStepSuccess(step);
		verifyTestFinish();
	}

	@Test
	public void shouldHandleExampleStepsInCombinationWithGivenStories() {
		// one story, one scenario, one given story, one example, one step
		Description givenStoryDescription = Description
				.createSuiteDescription("aGivenStory");
		scenarioDescription.addChild(givenStoryDescription);
		// one story, one scenario, one example, one step,
		Description example = addChildToScenario(keywords.examplesTableRow() + " "
				+ "row");
		Description step = Description.createTestDescription(this.getClass(),
				"Step");
		example.addChild(step);

		reporter = new JUnitScenarioReporter(notifier, ONE_GIVEN + ONE_STEP,
				rootDescription, keywords);

		reporter.beforeStory(story, false);
		reporter.beforeScenario(NAME_SCENARIO);
		reportGivenStoryEvents();
		reporter.example(null);
		reportStepSuccess(reporter);
		reportScenarioAndStoryFinish(reporter);

		verifyTestRunStarted();
		verifyStoryStarted();
		verifyScenarioStarted();
		verifyStepSuccess(givenStoryDescription);
		verifyStepSuccess(step);
		verifyTestFinish();
	}

	@Test
	public void shouldFailForPendingStepsAtBothStepAndScenarioLevelsIfConfigurationSaysSo() {
		Description child1 = addChildToScenario("child1");
		Description child2 = addChildToScenario("child2");

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);

		PendingStepStrategy strategy = new FailingUponPendingStep();
		reporter.usePendingStepStrategy(strategy);

		reportStoryAndScenarioStart(reporter);
		reporter.pending("child1");
		reporter.failed("child2",
				new UUIDExceptionWrapper(new Exception("FAIL")));
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestStarted(child1);
		verify(notifier, times(2)).fireTestFailure(Mockito.<Failure> anyObject());
		verify(notifier, times(1)).fireTestFinished(child1);
		verify(notifier, times(1)).fireTestFinished(child2);
	}

	@Test
	public void shouldIgnorePendingStepsIfConfigurationSaysSo() {
		Description child = addChildToScenario("child");

		reporter = new JUnitScenarioReporter(notifier, 3,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		reporter.pending("child");
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier, VerificationModeFactory.times(0)).fireTestStarted(
				child);
		verify(notifier).fireTestIgnored(child);
	}

	@Test
	public void shouldHandleFailuresInBeforeStories() {
		reporter = new JUnitScenarioReporter(notifier, 1,
				rootDescription, keywords);

		reporter.beforeStory(story, false);
		reporter.failed(NAME_STORY, new UUIDExceptionWrapper("Error Message",
				new RuntimeException("Cause")));
		ArgumentCaptor<Failure> argument = ArgumentCaptor
				.forClass(Failure.class);
		verify(notifier).fireTestFailure(argument.capture());
		assertThat(argument.getValue().getDescription(), is(storyDescription));
	}

	@Test
	public void shouldNotifyAboutNotAllowedScenario() {
		Description child1 = addChildToScenario("child");

		reporter = new JUnitScenarioReporter(notifier, ONE_STEP,
				rootDescription, keywords);

		reportStoryAndScenarioStart(reporter);
		reporter.scenarioNotAllowed(Mockito.mock(Scenario.class), "filter");
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestIgnored(child1);
		verify(notifier).fireTestIgnored(scenarioDescription);
	}

	private void reportScenarioAndStoryFinish(JUnitScenarioReporter reporter) {
		reporter.afterScenario();
		// test should not be finished until we send the final event
		verify(notifier, VerificationModeFactory.times(0)).fireTestRunFinished(
				Mockito.<Result> anyObject());
		reporter.afterStory(false);
	}

	private void reportStoryAndScenarioStart(JUnitScenarioReporter reporter) {
		reporter.beforeStory(story, false);
		reporter.beforeScenario(NAME_SCENARIO);
	}

	private void reportStepSuccess(JUnitScenarioReporter reporter) {
		reporter.beforeStep("child");
		reporter.successful("child");
	}

	private void reportStepFailure(JUnitScenarioReporter reporter) {
		reporter.beforeStep("child");
		reporter.failed("child",
				new UUIDExceptionWrapper(new Exception("FAIL")));
	}

	private void reportIgnorable(JUnitScenarioReporter reporter) {
		reporter.ignorable("!-- Comment");
	}

	private void reportGivenStoryEvents() {
		Story givenStory = new Story();
		givenStory.namedAs("aGivenStory");

		// Begin Given Story
		reporter.beforeStory(givenStory, true);
		reporter.beforeScenario("givenScenario");
		reporter.beforeStep("givenStep");
		reporter.successful("givenStep");
		reporter.afterScenario();
		reporter.afterStory(true);
		// End Given Story
	}

	private void verifyTestFinish() {
		verifyScenarioFinished();
		verifyStoryFinished();
		verifyTestRunFinished();
	}

	private void verifyTestStart() {
		verifyTestRunStarted();
		verifyStoryStarted();
		verifyScenarioStarted();
	}

	private void verifyStoryFinished() {
		verify(notifier).fireTestFinished(storyDescription);
	}

	private void verifyScenarioFinished() {
		verify(notifier).fireTestFinished(scenarioDescription);
	}

	private void verifyScenarioStarted() {
		verify(notifier).fireTestStarted(scenarioDescription);
	}

	private void verifyStoryStarted() {
		verify(notifier).fireTestStarted(storyDescription);
	}

	private void verifyStepSuccess(Description step) {
		verify(notifier).fireTestStarted(step);
		verify(notifier).fireTestFinished(step);
	}

	private void verifyTestRunFinished() {
		verify(notifier).fireTestRunFinished(Matchers.<Result> anyObject());
	}

	private void verifyTestRunStarted() {
		verify(notifier).fireTestRunStarted(Matchers.<Description> anyObject());
	}

	private Description addBeforeStories() {
		rootDescription = rootDescription.childlessCopy();
		Description beforeStories = Description.createTestDescription(
				Object.class, "BeforeStories");
		rootDescription.addChild(beforeStories);
		rootDescription.addChild(storyDescription);
		return beforeStories;
	}

	private Description addChildToScenario(String childName) {
		Description child = Description.createTestDescription(this.getClass(),
				childName);
		scenarioDescription.addChild(child);
		return child;
	}
}
