package de.codecentric.jbehave.junit.monitoring;

import java.util.HashSet;
import java.util.Set;

public class DescriptionTextUniquefier {

	Set<String> strings = new HashSet<>();

	public String getUniqueDescription(String junitSafeString) {
		while (strings.contains(junitSafeString)) {
			junitSafeString = junitSafeString + '\u200B'; // zero-width-space
		}
		strings.add(junitSafeString);
		return junitSafeString;
	}

}
