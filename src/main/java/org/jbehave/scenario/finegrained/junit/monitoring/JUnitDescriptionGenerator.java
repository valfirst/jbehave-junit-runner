package org.jbehave.scenario.finegrained.junit.monitoring;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.junit.runner.Description;

public class JUnitDescriptionGenerator {

    DescriptionTextUniquefier uniq = new DescriptionTextUniquefier();

    private int testCases;

	private List<StepCandidate> allCandidates = new ArrayList<StepCandidate>();
    
    public JUnitDescriptionGenerator(List<CandidateSteps> candidateSteps) {
		for (CandidateSteps candidateStep : candidateSteps) {
			allCandidates.addAll(candidateStep.listCandidates());
		}
	}

	public Description createDescriptionFrom(Scenario scenario) {
        Description scenarioDescription = Description.createSuiteDescription("Scenario: " + getJunitSafeString(scenario.getTitle()));
        if (hasGivenStories(scenario)) {
        	insertGivenStories(scenario, scenarioDescription);
        }
        
        if (hasExamples(scenario)) {
            insertDescriptionForExamples(scenario, scenarioDescription);
        } else {
            addScenarioSteps(scenario, scenarioDescription);
        }
        return scenarioDescription;
    }

	private void insertGivenStories(Scenario scenario,
			Description scenarioDescription) {
		for (String path: scenario.getGivenStories().getPaths()) {
			String name = path.substring(path.lastIndexOf("/")+1, path.length());
			scenarioDescription.addChild(
					Description.createSuiteDescription(getJunitSafeString(name))
			);
			testCases++;
		}
	}

	private boolean hasGivenStories(Scenario scenario) {
		return !scenario.getGivenStories().getPaths().isEmpty();
	}

	private boolean hasExamples(Scenario scenario) {
		ExamplesTable examplesTable = scenario.getExamplesTable();
		return examplesTable!= null && examplesTable.getRowCount() > 0;
	}

	private void insertDescriptionForExamples(Scenario scenario,
			Description scenarioDescription) {
		ExamplesTable examplesTable = scenario.getExamplesTable();
		int rowCount = examplesTable.getRowCount();
		for (int i = 1; i <= rowCount; i++) {
		    Description exampleRowDescription = Description.createSuiteDescription("Example: " + examplesTable.getRow(i-1), (Annotation[]) null);
		    scenarioDescription.addChild(exampleRowDescription);
		    addScenarioSteps(scenario, exampleRowDescription);
		}
	}

	private void addScenarioSteps(Scenario scenario, Description description) {
		for (String stringStep : scenario.getSteps()) {
			testCases++;
			for (StepCandidate step : allCandidates) {
				if (step.matches(stringStep)) {
					// JUnit and the Eclipse JUnit view needs to be touched/fixed in order to make the JUnit view
					// jump to the corresponding test accordingly. For now we have to live, that we end up in 
					// the correct class.
					if (stringStep.indexOf('\n') != -1) {
						stringStep = stringStep.substring(0, stringStep.indexOf('\n'));
					}
					Description testDescription = Description.createTestDescription(step.getStepsInstance().getClass(), getJunitSafeString(stringStep));
					description.addChild(testDescription);
				}
			}
		}
	}

    public Description createDescriptionFrom(Story story) {
        Description storyDescription = Description.createSuiteDescription(getJunitSafeString(story.getName()));
        List<Scenario> scenarios = story.getScenarios();
        for (Scenario scenario : scenarios) {
            storyDescription.addChild(createDescriptionFrom(scenario));
        }
        return storyDescription;
    }

    public String getJunitSafeString(String string) {
        return uniq.getUniqueDescription(string.replaceAll("\r", "\n").replaceAll("\n{2,}", "\n").replaceAll("\n", ", ").replaceAll("[\\(\\)]", "|"));
    }

	public int getTestCases() {
		return testCases;
	}

}
