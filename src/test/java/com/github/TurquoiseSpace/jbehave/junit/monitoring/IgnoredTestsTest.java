package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class IgnoredTestsTest.
 *
 * The purpose of this class is to see how an IDE behaves when there is a unit test that passes and a test is ignored.
 *
 * The result in IntelliJ 2017.2.5 Community Edition is that there are two lines of info reported about test outcomes:
 * (OK) IgnoredTestsTest
 *      (OK) shouldPass
 *
 * This implies to me that the jbehave-junit-runner should behave similarly that even if there are stories/scenarios
 * that are ignored (via meta tag filtering) then the highest level node should show (OK) and not (Pending).
 */
public class IgnoredTestsTest {

	@Test
	public void shouldPass() {
		// Nothing to do. Wish this test to pass.
		assertTrue(true);
	}

	@Ignore
	public void shouldBeIgnored() {
		// Nothing to do. Wish this test to be ignored.
		fail();
	}
}
