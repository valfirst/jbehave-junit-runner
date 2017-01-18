package de.codecentric.jbehave.junit.monitoring;

import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportAfterStories;
import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportBeforeStories;
import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportStepFailure;
import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportStoryAndScenarioFinished;
import static de.codecentric.jbehave.junit.monitoring.ReporterHelper.reportStoryAndScenarioStart;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Story;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JUnitStoryReporterTest {

	private static final String NAME_BEFORE_STORIES = "BeforeStories";
	private static final String NAME_AFTER_STORIES = "AfterStories";
	private static final String NAME_STORY = "story";
	private static final String NAME_SCENARIO = "scenario";
	private static final String NAME_STEP = "step";
	private static final String NAME_ROOT = "root";
	private static final int THREE_STEPS = 3;

	@Mock
	RunNotifier notifier;
	private Description rootDescription;
	private Description storyDescription;
	private Description scenarioDescription;
	private Description beforeStoryDescription;
	private Description afterStoryDescription;
	private Story story;
	private JUnitStoryReporter reporter;
	private ReporterVerifier reporterVerifier;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rootDescription = Description.createTestDescription(this.getClass(), NAME_ROOT);
		storyDescription = Description.createTestDescription(this.getClass(), NAME_STORY);
		scenarioDescription = Description.createTestDescription(this.getClass(), NAME_SCENARIO);
		beforeStoryDescription = Description.createTestDescription(this.getClass(), NAME_BEFORE_STORIES);
		afterStoryDescription = Description.createTestDescription(this.getClass(), NAME_AFTER_STORIES);
		rootDescription.addChild(beforeStoryDescription);
		rootDescription.addChild(storyDescription);
		rootDescription.addChild(afterStoryDescription);

		story = new Story();
		story.namedAs("story(" + this.getClass().getName() + ")");
		reporterVerifier = new ReporterVerifier(notifier, storyDescription, scenarioDescription);
	}

	@Test
	public void shouldRunStory() throws Exception {
		rootDescription = ReporterHelper.addBeforeStories(rootDescription, storyDescription);
		rootDescription = ReporterHelper.addAfterStories(rootDescription);
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);

		reportBeforeStories(reporter);
		reporterVerifier.testRunStarted();
		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporterVerifier.storyStarted();
		reportStoryAndScenarioFinished(reporter, story, NAME_SCENARIO);
		reportAfterStories(reporter);
		reporterVerifier.storyFinished();
		reporterVerifier.testRunFinished();
	}

	@Test
	public void shouldHandleFailedSteps() throws Exception {
		rootDescription = ReporterHelper.addBeforeStories(rootDescription, storyDescription);
		rootDescription = ReporterHelper.addAfterStories(rootDescription);
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);

		reportBeforeStories(reporter);
		reporterVerifier.testRunStarted();
		reportStoryAndScenarioStart(reporter, story, NAME_SCENARIO);
		reporterVerifier.storyStarted();
		reportStepFailure(reporter, NAME_STEP);
		verify(notifier).fireTestFailure(Matchers.<Failure> anyObject());
		reportAfterStories(reporter);
		reporterVerifier.storyFinished();
		reporterVerifier.testRunFinished();
	}

	@Test
	public void noEventsWereFiredForDryRun() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.dryRun();
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredAfterExamples() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.afterExamples();
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredBeforeExamples() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.beforeExamples(null, null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredExamples() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.example(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredFailedOutcomes() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.failedOutcomes(null, null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredIgnorable() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.ignorable(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredLifecyle() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.lifecyle(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredNarrative() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.narrative(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredPending() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.pending(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredPendingMethods() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.pendingMethods(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredRestarted() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.restarted(null, null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredScenarioMeta() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.scenarioMeta(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredScenarioNotAllowed() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.scenarioNotAllowed(null, null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredUsePendingStepStrategy() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.usePendingStepStrategy(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredSuccessful() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.successful(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredGivenStoriesWithdList() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.givenStories(new ArrayList<String>());
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredGivenStoriesWithGivenStory() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.givenStories(new GivenStories(null));
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredNotPerformeds() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.notPerformed(null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredStoryCancelled() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.storyCancelled(null, null);
		reporterVerifier.noEventsWereFired();
	}

	@Test
	public void noEventsWereFiredStoryNotAllowed() {
		reporter = new JUnitStoryReporter(notifier, THREE_STEPS, rootDescription);
		reporter.storyNotAllowed(null, null);
		reporterVerifier.noEventsWereFired();
	}
}
