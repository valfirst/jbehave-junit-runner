package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import java.util.HashSet;
import java.util.Set;

public class DescriptionTextUniquefier {

	private final Set<String> uniqueDescriptions = new HashSet<>();

	public String getUniqueDescription(String junitSafeString) {
		String uniqueDescription = junitSafeString;
		while (uniqueDescriptions.contains(uniqueDescription)) {
			uniqueDescription += '\u200B'; // zero-width-space
		}
		uniqueDescriptions.add(uniqueDescription);
		return uniqueDescription;
	}

}
