package de.codecentric.jbehave.junit.monitoring;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Matchers;

/**
 * Via junit {@link RunNotifier} we verify methods call.
 *
 * @author Michal Bocek
 * @since 01/08/2016
 */
public class ReporterVerifier {

	private RunNotifier notifier;
	private Description storyDescription;
	private Description scenarioDescription;

	public ReporterVerifier(RunNotifier notifier, Description storyDescription, Description scenarioDescription) {
		this.notifier = notifier;
		this.storyDescription = storyDescription;
		this.scenarioDescription = scenarioDescription;
	}

	/**
	 * Method {@link RunNotifier#fireTestRunFinished(Result)} was called.
	 */
	public void testRunFinished() {
		verify(notifier).fireTestRunFinished(Matchers.<Result> anyObject());
	}
	
	/**
	 * Method {@link RunNotifier#fireTestRunStarted(Description)} was called
	 */
	public void testRunStarted() {
		verify(notifier).fireTestRunStarted(Matchers.<Description> anyObject());
	}
	
	/**
	 * Method {@link RunNotifier#fireTestStarted(Description)} was called
	 */
	public void storyStarted() {
		verify(notifier).fireTestStarted(storyDescription);
	}

	/**
	 * Method {@link RunNotifier#fireTestFinished(Description)} per story was called
	 */
	public void storyFinished() {
		verify(notifier).fireTestFinished(storyDescription);
	}

	/**
	 * Method {@link RunNotifier#fireTestStarted(Description)} per story was called
	 */
	public void scenarioStarted() {
		verify(notifier).fireTestStarted(scenarioDescription);
	}
	
	/**
	 * Method {@link RunNotifier#fireTestStarted(Description)} per scenario was called
	 */
	public void scenarioFinished() {
		verify(notifier).fireTestFinished(scenarioDescription);
	}

	/**
	 * Methods {@link RunNotifier#fireTestStarted(Description)} and {@link RunNotifier#fireTestFinished(Description)} was called
	 */
	public void stepSuccess(Description step) {
		verify(notifier).fireTestStarted(step);
		verify(notifier).fireTestFinished(step);
	}

	public void noEventsWereFired() {
		verify(notifier, never()).fireTestRunStarted(Matchers.<Description> anyObject());
		verify(notifier, never()).fireTestRunFinished(Matchers.<Result> anyObject());

		verify(notifier, never()).fireTestStarted(Matchers.<Description> anyObject());
		verify(notifier, never()).fireTestFinished(Matchers.<Description> anyObject());

		verify(notifier, never()).fireTestIgnored(Matchers.<Description> anyObject());
		verify(notifier, never()).fireTestAssumptionFailed(Matchers.<Failure> anyObject());
		verify(notifier, never()).fireTestFailure(Matchers.<Failure> anyObject());
	}
}
