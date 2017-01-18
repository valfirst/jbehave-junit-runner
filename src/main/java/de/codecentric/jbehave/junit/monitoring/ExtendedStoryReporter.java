package de.codecentric.jbehave.junit.monitoring;

import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.reporters.StoryReporter;

/**
 * Extended {@link StoryReporter}.
 * 
 * @author Michal Bocek
 * @since 28/7/16
 */
public interface ExtendedStoryReporter extends StoryReporter {
	
	/**
	 * Use pending step strategy.
	 *
	 * @param strategy the strategy
	 */
	void usePendingStepStrategy(PendingStepStrategy strategy);
}
