package org.jbehave.scenario.finegrained.junit.monitoring;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;


public class JUnitScenarioReporterTest {

    @Mock
    RunNotifier notifier;
    
    @Before
    public void setUp() {
	MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters() throws Exception {
	
	Description storyDescription = Description.createTestDescription(this.getClass(), "gtdaddy");
	Description scenarioDescription = Description.createTestDescription(this.getClass(), "daddy");
	Description child1 = Description.createTestDescription(this.getClass(), "child");
	Description child2 = Description.createTestDescription(this.getClass(), "child.");
	Description child3 = Description.createTestDescription(this.getClass(), "child..");
	storyDescription.addChild(scenarioDescription);
	scenarioDescription.addChild(child1);
	scenarioDescription.addChild(child2);
	scenarioDescription.addChild(child3);
	
	
	JUnitScenarioReporter reporter = new JUnitScenarioReporter(notifier, storyDescription);
	reporter.beforeScenario("daddy");
	reporter.successful("child");
	reporter.successful("child");
	reporter.successful("child");
	verify(notifier).fireTestStarted(child1);
	verify(notifier).fireTestStarted(child2);
	verify(notifier).fireTestStarted(child3);
	
    }
    

}
