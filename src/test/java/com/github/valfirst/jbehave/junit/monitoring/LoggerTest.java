package com.github.valfirst.jbehave.junit.monitoring;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoggerTest {

	@Mock
	private PrintStream logStream;

	@InjectMocks
	private Logger logger;

	@Before
	public void setUp() {
		System.clearProperty(Logger.PROP_JJM_LOGLEVEL);
	}

	@Test
	public void shouldLogNothingForLoglevelNull() {
		logger.info("Should be lost");
		verifyZeroInteractions(logStream);
	}

	@Test
	public void shouldLogNothingForUnknownLogLevle() {
		setLevel("invalid");
		logger.info("Should be lost");
		verifyZeroInteractions(logStream);
	}

	@Test
	public void shoudLogNothingForLogLevelNone() {
		setLevel("NONE");
		logger.info("lost info");
		logger.debug("lost debug");
		verifyZeroInteractions(logStream);
	}

	@Test
	public void shouldLogInfoForInfo() {
		setLevel("INFO");
		String msg = "I am very informative";
		logger.info(msg);
		verify(logStream).println("INFO: " + msg);
	}

	@Test
	public void shouldLogDebugForDebug() {
		setLevel("DEBUG");
		String msg = "hunting for insects";
		logger.debug(msg);
		verify(logStream).println("DEBUG: " + msg);
	}

	@Test
	public void shouldLogInfoForDebug() {
		setLevel("DEBUG");
		String msg = "info on debug";
		logger.info(msg);
		verify(logStream).println("INFO: " + msg);
	}

	@Test
	public void shouldHandleStringVars() {
		setLevel("INFO");
		logger.info("I {} very {}", "am", "informative");
		verify(logStream).println("INFO: I am very informative");
	}

	@Test
	public void shouldHandleNullVars() {
		setLevel("INFO");
		logger.info("{} is the {} of most {}", null, "root", "evil");
		verify(logStream).println("INFO: null is the root of most evil");
	}

	@Test
	public void shouldHandleNonStringVars() {
		setLevel("INFO");
		List<Long> longs = new ArrayList<>();
		longs.add(1L);
		longs.add(2L);
		longs.add(3L);
		logger.info("{} is not in {}", Integer.valueOf(0), longs);
		verify(logStream).println("INFO: 0 is not in [1, 2, 3]");
	}

	private void setLevel(String aLevelToSet) {
		System.setProperty(Logger.PROP_JJM_LOGLEVEL, aLevelToSet);
	}
}
