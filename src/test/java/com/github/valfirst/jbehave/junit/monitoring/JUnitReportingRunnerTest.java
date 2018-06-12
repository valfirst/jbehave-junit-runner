package com.github.valfirst.jbehave.junit.monitoring;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class JUnitReportingRunnerTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldPrepareEmbedder() {
		EmbedderControls ec = mock(EmbedderControls.class);
		when(ec.doIgnoreFailureInStories(Mockito.anyBoolean())).thenReturn(ec);
		when(ec.doIgnoreFailureInView(Mockito.anyBoolean())).thenReturn(ec);
		when(ec.useThreads(Mockito.anyInt())).thenReturn(ec);
		Embedder e = mock(Embedder.class);
		when(e.embedderControls()).thenReturn(ec);
		EmbedderControls recommendedControls = JUnitReportingRunner.recommendedControls(e);
		assertThat(recommendedControls, is(ec));
		verify(ec).doIgnoreFailureInView(true);
		verify(ec).doIgnoreFailureInStories(true);
		verify(ec).useThreads(1);
	}
}
