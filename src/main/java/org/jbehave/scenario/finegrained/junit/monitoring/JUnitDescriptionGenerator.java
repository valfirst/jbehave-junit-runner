package org.jbehave.scenario.finegrained.junit.monitoring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
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

	private final List<CandidateSteps> candidateSteps;

	private List<StepCandidate> allCandidates = new ArrayList<StepCandidate>();
    
    public JUnitDescriptionGenerator(List<CandidateSteps> candidateSteps) {
		this.candidateSteps = candidateSteps;
		for (CandidateSteps candidateStep : candidateSteps) {
			allCandidates.addAll(candidateStep.listCandidates());
		}
	}

	public Description createDescriptionFrom(Scenario scenario) {
        Description scenarioDescription = Description.createSuiteDescription("Scenario: " + scenario.getTitle());
        if (hasExamples(scenario)) {
            insertDescriptionForExamples(scenario, scenarioDescription);
        } else {
            addScenarioSteps(scenario, scenarioDescription);
        }
        return scenarioDescription;
    }

	private boolean hasExamples(Scenario scenario) {
		return scenario.getExamplesTable() != null;
	}

	private void insertDescriptionForExamples(Scenario scenario,
			Description scenarioDescription) {
		ExamplesTable examplesTable = scenario.getExamplesTable();
		int rowCount = examplesTable.getRowCount();
		for (int i = 1; i <= rowCount; i++) {
		    Collection<String> rowValues = examplesTable.getRow(i-1).values();
		    Description exampleRowDescription = Description.createSuiteDescription("Example: " + rowValues, (Annotation[]) null);
		    scenarioDescription.addChild(exampleRowDescription);
		    addScenarioSteps(scenario, exampleRowDescription);
		}
	}

	private void addScenarioSteps(Scenario scenario, Description description) {
		for (String stringStep : scenario.getSteps()) {
			testCases++;
			for (StepCandidate step : allCandidates) {
				if (step.matches(stringStep)) {
					Method method = step.getMethod();
					// JUnit and the Eclipse JUnit view needs to be touched/fixed in order to make the JUnit view
					// jump to the corresponding test accordingnly. For now we have to live, that we end up in 
					// the correct class.
					Description testDescription = Description.createTestDescription(method.getDeclaringClass(), getJunitSafeString(stringStep));
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
        return uniq.getUniqueDescription(string.replaceAll("\n", ", ").replaceAll("[\\(\\)]", "|"));
    }

	public int getTestCases() {
		return testCases;
	}

}
