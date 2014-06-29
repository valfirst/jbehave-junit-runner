package de.codecentric.jbehave.junit.monitoring;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parsers.RegexStoryParser;
import org.junit.runner.RunWith;

@RunWith(JUnitReportingRunner.class)
public class ExampleScenarioJUnitStoriesLocalized extends ExampleScenarioJUnitStories {

	@Override
	public Configuration configuration() {
		LocalizedKeywords keywords = new LocalizedKeywords(Locale.GERMAN);
		return super.configuration()
				.useKeywords(keywords)
				.useStoryParser(new RegexStoryParser(keywords));
	}

	@Override
	protected List<String> storyPaths() {
		return Arrays.asList("de/codecentric/jbehave/junit/monitoring/Multiplication_de.story");
	}
}
