package de.codecentric.jbehave.junit.monitoring;

import junit.framework.TestCase;

import org.jbehave.core.ConfigurableEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JUnitReportingRunnerTest extends TestCase {

	@Mock
	private Class<? extends ConfigurableEmbedder> testClass;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGenerateDescriptionWithoutCrossReference()
			throws Throwable {

		// TODO ... no idea how this should be tested!
		// new JUnitReportingRunner(testClass);
		// verify
		// configuration.useStepMonitor(nullStepMonitor);
		// step.configuration().useStepMonitor(nullStepMonitor);
		// storyDescriptions = buildDescriptionFromStories();
		// configuration.useStepMonitor(usedStepMonitor);
		// for (CandidateSteps step : candidateSteps) {
		// step.configuration().useStepMonitor(usedStepMonitor);
		// }
		// candidateSteps = embedder.stepsFactory().createCandidateSteps();

	}
}
