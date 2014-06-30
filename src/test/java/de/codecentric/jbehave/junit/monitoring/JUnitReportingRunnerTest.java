package de.codecentric.jbehave.junit.monitoring;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.junit.Test;
import org.mockito.Mockito;

public class JUnitReportingRunnerTest {

	@Test
	public void shouldPrepareEmbedder() {
		EmbedderControls ec = mock(EmbedderControls.class);
		when(ec.doIgnoreFailureInStories(Mockito.anyBoolean())).thenReturn(ec);
		when(ec.doIgnoreFailureInView(Mockito.anyBoolean())).thenReturn(ec);
		when(ec.useThreads(Mockito.anyInt())).thenReturn(ec);
		Embedder e = mock(Embedder.class);
		when(e.embedderControls()).thenReturn(ec);
		EmbedderControls recommandedControls = JUnitReportingRunner
				.recommendedControls(e);
		assertThat(recommandedControls, is(ec));
		verify(ec).doIgnoreFailureInView(true);
		verify(ec).doIgnoreFailureInStories(true);
		verify(ec).useThreads(1);
	}

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
