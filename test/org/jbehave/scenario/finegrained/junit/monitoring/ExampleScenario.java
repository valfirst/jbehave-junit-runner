package org.jbehave.scenario.finegrained.junit.monitoring;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.finegrained.junit.monitoring.annotations.UseConfiguration;
import org.jbehave.scenario.finegrained.junit.monitoring.annotations.UseSteps;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.junit.runner.RunWith;



@RunWith(JUnitReportingRunner.class)
@UseSteps(ExampleSteps.class)
@UseConfiguration(ExampleScenario.MyJBehaveConfiguration.class)
public class ExampleScenario extends JUnitScenario {

    public ExampleScenario(final ScenarioReporter reporter) {
	super(new MyJBehaveConfiguration() {
	    @Override
	    public ScenarioReporter forReportingScenarios() {
	        return reporter;
	    }
	}, new ExampleSteps());
    }
    
    
    public static class MyJBehaveConfiguration extends PropertyBasedConfiguration {
        @Override
        public ClasspathScenarioDefiner forDefiningScenarios() {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            
            return new ClasspathScenarioDefiner(new UnderscoredCamelCaseResolver(), new PatternScenarioParser(this),
                    contextClassLoader);
        }
    }

}
