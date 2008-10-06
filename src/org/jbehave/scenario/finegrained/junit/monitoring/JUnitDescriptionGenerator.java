package org.jbehave.scenario.finegrained.junit.monitoring;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.definition.ScenarioDefinition;
import org.jbehave.scenario.definition.StoryDefinition;
import org.jbehave.scenario.steps.CandidateStep;
import org.jbehave.scenario.steps.Steps;
import org.junit.runner.Description;

public class JUnitDescriptionGenerator {
    
    public Description createDescriptionFrom(ScenarioDefinition scenario, Steps... candidateSteps) {
    	Description scenarioDescription = Description.createTestDescription(candidateSteps[0].getClass(), scenario.getTitle());
    	DescriptionTextUniquefier uniquefier = new DescriptionTextUniquefier();
        for (String stringStep : scenario.getSteps()) {
            for (Steps candidates : candidateSteps) {
                for (CandidateStep candidate : candidates.getSteps()) {
                    if (candidate.matches(stringStep)) {
                	String uniqueString = uniquefier.getUniqueDescription(getJunitSafeString(stringStep));
			scenarioDescription.addChild(Description.createTestDescription(candidates.getClass(), uniqueString + " - Scenario: " + scenario.getTitle() + ""));
                    }
                }
            }
        }
        return scenarioDescription;
    }

    public Description createDescriptionFrom(StoryDefinition story,
	    Steps candidateSteps, Class<? extends JUnitScenario> testClass) {
	Description storyDescription = Description.createSuiteDescription(testClass);
	for (ScenarioDefinition definition : story.getScenarios()) {
	    storyDescription.addChild(createDescriptionFrom(definition,
			    candidateSteps));
	}
	return storyDescription;
    }	
    
    public static String getJunitSafeString(String string) {
	return string.replaceAll("\n", ", ").replaceAll("[\\(\\)]", "|");
    }
}
