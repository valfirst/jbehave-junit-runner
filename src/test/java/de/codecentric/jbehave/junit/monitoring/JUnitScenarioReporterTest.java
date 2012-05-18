package de.codecentric.jbehave.junit.monitoring;

import static org.mockito.Mockito.verify;

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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public class JUnitScenarioReporterTest {

	@Mock
	RunNotifier notifier;
	private Description rootDescription;
	private Description storyDescription;
	private Description scenarioDescription;
	private Story story;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rootDescription = Description.createTestDescription(this
				.getClass(), "root");
		storyDescription = Description.createTestDescription(this
				.getClass(), "story");
		rootDescription.addChild(storyDescription);
		scenarioDescription = Description.createTestDescription(
				this.getClass(), "scenario");
		storyDescription.addChild(scenarioDescription);

		story = new Story();
		story.namedAs("story("+this.getClass().getName()+")");
	}

	@Test
	public void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters()
			throws Exception {

		Description child1 = addChildToScenario("child");
		Description child2 = addChildToScenario("child.");
		Description child3 = addChildToScenario("child..");

		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);
		
		reportDefaultScenarioStart(reporter);
		reportStepSuccess(reporter);
		reportStepSuccess(reporter);
		reportStepSuccess(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestStarted(child1);
		verify(notifier).fireTestFinished(child1);
		verify(notifier).fireTestStarted(child2);
		verify(notifier).fireTestFinished(child2);
		verify(notifier).fireTestStarted(child3);
		verify(notifier).fireTestFinished(child3);
	}

	@Test
	public void shouldHandleFailedSteps()
			throws Exception {
		
		Description child1 = addChildToScenario("child");
		
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				1, rootDescription);
		
		reportDefaultScenarioStart(reporter);
		reportStepFailure(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestStarted(child1);
		verify(notifier).fireTestFailure(Matchers.<Failure>anyObject());
	}
	
	@Test 
	public void shouldHandleIgnorableSteps() throws Exception {
		Description comment = addChildToScenario("!-- Comment");
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier, 1, rootDescription);
		
		reportDefaultScenarioStart(reporter);
		verifyStoryStarted();
		verifyScenarioStarted();
		reportIgnorable(reporter);
		verify(notifier).fireTestIgnored(comment);
		
	}

	@Test
	public void shouldNotifyAboutBeforeStories() {
		rootDescription = rootDescription.childlessCopy();
		Description beforeStories = Description.createTestDescription(Object.class, "BeforeStories");
		rootDescription.addChild(beforeStories);
		rootDescription.addChild(storyDescription);
		
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);

		Story beforeStoriesStory = new Story();
		beforeStoriesStory.namedAs("BeforeStories");
		
		reporter.beforeStory(beforeStoriesStory, false);
		reporter.afterStory(false);
		verify(notifier).fireTestStarted(beforeStories);
		verify(notifier).fireTestFinished(beforeStories);
		
	}

	@Test
	public void shouldNotifyAboutAfterStories() {
		Description afterStories = Description.createTestDescription(Object.class, "AfterStories");
		rootDescription.addChild(afterStories);
		
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);
		
		Story afterStoriesStory = new Story();
		afterStoriesStory.namedAs("AfterStories");
		
		reporter.beforeStory(story, false);
		reporter.afterStory(false);
		reporter.beforeStory(afterStoriesStory, false);
		reporter.afterStory(false);
		verify(notifier).fireTestStarted(afterStories);
		verify(notifier).fireTestFinished(afterStories);
		
	}
	
	@Test
	public void shouldNotifyGivenStory() {
		
		Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
		scenarioDescription.addChild(givenStoryDescription);
		Description child = addChildToScenario("child");

		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);
		

		Story givenStory = new Story();
		givenStory.namedAs("aGivenStory");
		
		reporter.beforeStory(story, false);
		reporter.beforeScenario(scenarioDescription.getDisplayName());
		// Begin Given Story
		reporter.beforeStory(givenStory, true);
		reporter.beforeScenario("givenScenario");
		reporter.beforeStep("givenStep");
		reporter.successful("givenStep");
		reporter.afterScenario();
		reporter.afterStory(true);
		// End Given Story
		reportStepSuccess(reporter);
		reportDefaultScenarioFinish(reporter);
		
		verifyStandardStart();
		verify(notifier).fireTestStarted(givenStoryDescription);
		verify(notifier).fireTestFinished(givenStoryDescription);
		verify(notifier).fireTestStarted(child);
		verify(notifier).fireTestFinished(child);
		verifyStandardFinish();
	}

	@Test
	public void shouldNotifyCompositeSteps() {
		// one story, one scenario, one step, two composite steps
		Description child = addChildToScenario("child");
		Description comp1 = Description.createTestDescription(this.getClass(), "comp1");
		child.addChild(comp1);
		Description comp2 = Description.createTestDescription(this.getClass(), "comp2");
		child.addChild(comp2);
	
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				4, rootDescription);
		
		reportDefaultScenarioStart(reporter);
		reportStepSuccess(reporter);
		reporter.beforeStep("comp1");
		reporter.successful("comp1");
		reporter.beforeStep("comp2");
		reporter.successful("comp2");
		reportDefaultScenarioFinish(reporter);
		
		verifyStandardStart();
		verify(notifier).fireTestStarted(child);
		verify(notifier).fireTestStarted(comp1);
		verify(notifier).fireTestFinished(comp1);
		verify(notifier).fireTestStarted(comp2);
		verify(notifier).fireTestFinished(comp2);
		verify(notifier).fireTestFinished(child);
		verifyStandardFinish();
	}

	@Test
	public void shouldPrepareExampleStepsBeforeScenario() {
		// one story, one scenario, one example, one step, 
		Description example = addChildToScenario(JUnitDescriptionGenerator.EXAMPLE_DESCRIPTION_PREFIX + "row");
		Description step = Description.createTestDescription(this.getClass(), "Step");
		example.addChild(step);
		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier, 2, rootDescription);
		reportDefaultScenarioStart(reporter);
		reporter.example(null);
		reportStepSuccess(reporter);
		reportDefaultScenarioFinish(reporter);
		
		verifyStandardStart();
		verify(notifier).fireTestStarted(step);
		verify(notifier).fireTestFinished(step);
		verifyStandardFinish();
	}

	@Test
	public void shouldFailForPendingStepsIfConfigurationSaysSo() {
		Description child = addChildToScenario("child");

		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);
		
		PendingStepStrategy strategy = new FailingUponPendingStep();
		reporter.usePendingStepStrateg(strategy);
		
		reportDefaultScenarioStart(reporter);
		reporter.beforeStep("child");
		reporter.pending("child");
		verifyStoryStarted();
		verifyScenarioStarted();
		verify(notifier).fireTestStarted(child);
		verify(notifier).fireTestFailure(Mockito.<Failure>anyObject());
	}

	private void reportDefaultScenarioFinish(JUnitScenarioReporter reporter) {
		reporter.afterScenario();
		reporter.afterStory(false);
	}

	private void reportDefaultScenarioStart(JUnitScenarioReporter reporter) {
		reporter.beforeStory(story, false);
		reporter.beforeScenario("scenario");
	}
	
	private void verifyStandardFinish() {
		verifyScenarioFinished();
		verifyStoryFinished();
		verifyTestRunFinished();
	}

	private void verifyStandardStart() {
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

	private void verifyTestRunFinished() {
		verify(notifier).fireTestRunFinished(Matchers.<Result>anyObject());
	}

	private void verifyTestRunStarted() {
		verify(notifier).fireTestRunStarted(Matchers.<Description>anyObject());
	}
	
	private Description addChildToScenario(String childName) {
		
		Description child = Description.createTestDescription(this.getClass(),
				childName);
		scenarioDescription.addChild(child);
		return child;
	}

	private void reportStepSuccess(JUnitScenarioReporter reporter) {
		reporter.beforeStep("child");
		reporter.successful("child");
	}

	private void reportStepFailure(JUnitScenarioReporter reporter) {
		reporter.beforeStep("child");
		reporter.failed("child", new UUIDExceptionWrapper(new Exception("FAIL")));
	}
	
	private void reportIgnorable(JUnitScenarioReporter reporter) {
		reporter.ignorable("!-- Comment");
	}
	
}
