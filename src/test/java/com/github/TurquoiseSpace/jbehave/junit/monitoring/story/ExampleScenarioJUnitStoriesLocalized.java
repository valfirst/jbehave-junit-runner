package com.github.TurquoiseSpace.jbehave.junit.monitoring.story;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.github.TurquoiseSpace.jbehave.junit.monitoring.JUnitReportingRunner;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.runner.RunWith;

@RunWith(JUnitReportingRunner.class)
public class ExampleScenarioJUnitStoriesLocalized extends ExampleScenarioJUnitStories
{

	@Override
	public Configuration configuration() {
		return super.configuration().useKeywords(new LocalizedKeywords(Locale.GERMAN));
	}

	@Override
	public List<String> storyPaths() {
		return Collections.singletonList("com/github/TurquoiseSpace/jbehave/junit/monitoring/story/Multiplication_de.story");
	}
}
