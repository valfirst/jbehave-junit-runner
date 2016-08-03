package de.codecentric.jbehave.junit.monitoring;

/**
 * @author Michal Bocek
 * @since 27/07/16
 */
public enum JUnitReportLevel {
	STORY, STEP;
	
	public boolean isAtMinimumStoryLevel() {
		return JUnitReportLevel.STORY.compareTo(this) <= 0;
	}

	public boolean isAtMinimumStepLevel() {
		return JUnitReportLevel.STEP.compareTo(this) <= 0;
	}
}
