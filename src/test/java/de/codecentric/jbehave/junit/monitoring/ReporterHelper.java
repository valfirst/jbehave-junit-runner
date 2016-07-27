package de.codecentric.jbehave.junit.monitoring;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.runner.Description;

/**
 * Generate calls to {@link StoryReporter}.
 * @author Michal Bocek
 * @since 01/08/2016
 */
public final class ReporterHelper {

	private static final String BEFORE_STORIES = "BeforeStories";
	private static final String AFTER_STORIES = "AfterStories";

	private ReporterHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Add before stories.
	 * Create childless copy from rootDescription and add BeforeStories and Story child.
	 *
	 * @param rootDescription the root description
	 * @param storyDescription the story description
	 * @return the description
	 */
	public static Description addBeforeStories(Description rootDescription, Description storyDescription) {
		rootDescription = rootDescription.childlessCopy();
		Description beforeStories = Description.createTestDescription(Object.class, BEFORE_STORIES);
		rootDescription.addChild(beforeStories);
		rootDescription.addChild(storyDescription);
		return rootDescription;
	}
	
	/**
	 * Add after stories.
	 *
	 * @param rootDescription the root description
	 * @param storyDescription the story description
	 * @return the description
	 */
	public static Description addAfterStories(Description rootDescription) {
		Description afterStories = Description.createTestDescription(Object.class, AFTER_STORIES);
		rootDescription.addChild(afterStories);
		return rootDescription;
	}


	/**
	 * Report given story events.
	 *
	 * @param reporter the reporter
	 */
	public static void reportGivenStoryEvents(JUnitScenarioReporter reporter) {
		Story givenStory = new Story();
		givenStory.namedAs("aGivenStory");

		// Begin Given Story
		reporter.beforeStory(givenStory, true);
		reporter.beforeScenario("givenScenario");
		reporter.beforeStep("givenStep");
		reporter.successful("givenStep");
		reporter.afterScenario();
		reporter.afterStory(true);
		// End Given Story
	}

	/**
	 * Gets the before stories description from root description.
	 *
	 * @param rootDescription the root description
	 * @return the before stories
	 */
	public static Description getBeforeStories(Description rootDescription) {
		for (Description child : rootDescription.getChildren()) {
			if (BEFORE_STORIES.equals(child.getMethodName())) {
				return child;
			}
		} 
		throw new IllegalStateException("Descrioption doesn't contains BeforeStories step!");
	}

	/**
	 * Report BeforeStories as two calls to reporter (beforeStory/afterStory).
	 *
	 * @param reporter the reporter
	 */
	public static void reportBeforeStories(StoryReporter reporter) {
		reporter.beforeStory(new Story(BEFORE_STORIES), false);
		reporter.afterStory(false);
	}
	
	/**
	 * Report AfterStories as two calls to reporter (beforeStory/afterStory).
	 *
	 * @param reporter the reporter
	 */
	public static void reportAfterStories(StoryReporter reporter) {
		reporter.beforeStory(new Story(AFTER_STORIES), false);
		reporter.afterStory(false);
	}
	
	/**
	 * Report story and scenario start.
	 *
	 * @param reporter the reporter
	 * @param story the story
	 * @param scenarioName the scenario name
	 */
	public static void reportStoryAndScenarioStart(StoryReporter reporter, Story story, String scenarioName) {
		reporter.beforeStory(story, false);
		reporter.beforeScenario(scenarioName);
	}
	
	/**
	 * Report story and scenario finished.
	 *
	 * @param reporter the reporter
	 * @param story the story
	 * @param scenarioName the scenario name
	 */
	public static void reportStoryAndScenarioFinished(StoryReporter reporter, Story story, String scenarioName) {
		reporter.afterScenario();
		reporter.afterStory(false);
	}
	
	public static void reportStepSuccess(StoryReporter reporter, String stepName) {
		reporter.beforeStep(stepName);
		reporter.successful(stepName);
	}
	

	public static void reportIgnorable(StoryReporter reporter) {
		reporter.ignorable("!-- Comment");
	}
	

	public static void reportStepFailure(StoryReporter reporter, String stepName) {
		reporter.beforeStep(stepName);
		reporter.failed(stepName, new UUIDExceptionWrapper(new Exception("FAIL")));
	}
	

	public static Description addChildToScenario(Description scenarioDescription, String childName) {
		Description child = Description.createTestDescription(Object.class, childName);
		scenarioDescription.addChild(child);
		return child;
	}
}
