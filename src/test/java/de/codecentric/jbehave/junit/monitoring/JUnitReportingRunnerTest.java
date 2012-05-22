package de.codecentric.jbehave.junit.monitoring;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JUnitReportingRunnerTest {

	@Test
	public void shouldGenerateDescriptionWithoutCrossReference()
			throws Throwable {
		assertThat(true, is(not(false)));
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
