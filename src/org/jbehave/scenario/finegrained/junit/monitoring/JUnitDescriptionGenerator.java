package org.jbehave.scenario.finegrained.junit.monitoring;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.runner.Description;

public class JUnitDescriptionGenerator {

    static DescriptionTextUniquefier uniq = new DescriptionTextUniquefier();

    public Description createDescriptionFrom(Scenario scenario) {
        Description scenarioDescription = Description.createSuiteDescription("Scenario: " + scenario.getTitle());
        ExamplesTable examplesTable = scenario.getExamplesTable();
        if (examplesTable != null) {
            int rowCount = examplesTable.getRowCount();
            for (int i = 1; i <= rowCount; i++) {
                Collection<String> rowValues = examplesTable.getRow(i-1).values();
                Description exampleRowDescription = Description.createSuiteDescription("Example: " + rowValues, (Annotation[]) null);
                scenarioDescription.addChild(exampleRowDescription);
                for (String stringStep : scenario.getSteps()) {
                    exampleRowDescription.addChild(Description.createTestDescription(Object.class, getJunitSafeString(stringStep)));
                }
            }
        } else {
            for (String stringStep : scenario.getSteps()) {
                scenarioDescription.addChild(Description.createTestDescription(Object.class, getJunitSafeString(stringStep)));
            }
        }
        return scenarioDescription;
    }

    public Description createDescriptionFrom(Story story) {
        Description storyDescription = Description.createSuiteDescription(getJunitSafeString(story.getName()));
        List<Scenario> scenarios = story.getScenarios();
        for (Scenario scenario : scenarios) {
            storyDescription.addChild(createDescriptionFrom(scenario));
        }
        return storyDescription;
    }

    public static String getJunitSafeString(String string) {
        return uniq.getUniqueDescription(string.replaceAll("\n", ", ").replaceAll("[\\(\\)]", "|"));
    }
}
