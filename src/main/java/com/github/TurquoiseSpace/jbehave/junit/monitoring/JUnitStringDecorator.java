package com.github.TurquoiseSpace.jbehave.junit.monitoring;

/**
 * @author Valery_Yatsynovich
 */
public final class JUnitStringDecorator
{
	private static final char ONE_DOT_LEADER = '\u2024';

	private JUnitStringDecorator() {
		// Utility class
	}

	public static String getJunitSafeString(String string) {
		return string.replace('.', ONE_DOT_LEADER)
				.replaceAll("[\r\n]+", ", ")
				.replaceAll("[()]", "|");
	}
}
