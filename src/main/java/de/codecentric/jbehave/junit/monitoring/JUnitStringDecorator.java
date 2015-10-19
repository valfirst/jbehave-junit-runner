package de.codecentric.jbehave.junit.monitoring;

/**
 * @author Valery_Yatsynovich
 */
public final class JUnitStringDecorator
{
	private JUnitStringDecorator()
	{
		// Utility class
	}

	public static String getJunitSafeString(String string) {
		return replaceLinebreaks(string).replaceAll("[\\(\\)]", "|");
	}

	private static String replaceLinebreaks(String string) {
		return string.replaceAll("\r", "\n").replaceAll("\n+", ", ");
	}
}
