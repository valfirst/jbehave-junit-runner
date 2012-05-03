package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.mockito.Mockito.verify;

import org.jbehave.core.model.Story;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class JUnitScenarioReporterTest {

	@Mock
	RunNotifier notifier;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters()
			throws Exception {

		Description rootDescription = Description.createTestDescription(this
				.getClass(), "root");
		Description storyDescription = Description.createTestDescription(this
				.getClass(), "gtdaddy");
		Description scenarioDescription = Description.createTestDescription(
				this.getClass(), "daddy");
		Description child1 = Description.createTestDescription(this.getClass(),
				"child");
		Description child2 = Description.createTestDescription(this.getClass(),
				"child.");
		Description child3 = Description.createTestDescription(this.getClass(),
				"child..");
		rootDescription.addChild(storyDescription);
		storyDescription.addChild(scenarioDescription);
		scenarioDescription.addChild(child1);
		scenarioDescription.addChild(child2);
		scenarioDescription.addChild(child3);

		JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier,
				3, rootDescription);
		
		Story story = new Story();
		story.namedAs("gtdaddy("+this.getClass().getName()+")");
		reporter.beforeStory(story, false);
		reporter.beforeScenario("daddy");
		reportStep(reporter);
		reportStep(reporter);
		reportStep(reporter);
		verify(notifier).fireTestStarted(storyDescription);
		verify(notifier).fireTestStarted(scenarioDescription);
		verify(notifier).fireTestStarted(child1);
		verify(notifier).fireTestFinished(child1);
		verify(notifier).fireTestStarted(child2);
		verify(notifier).fireTestFinished(child2);
		verify(notifier).fireTestStarted(child3);
		verify(notifier).fireTestFinished(child3);

	}

	private void reportStep(JUnitScenarioReporter reporter) {
		reporter.beforeStep("child");
		reporter.successful("child");
	}

}
