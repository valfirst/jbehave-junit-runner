package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Valery_Yatsynovich
 */
public class JUnitStringDecoratorTest
{
	@Test
	public void shouldReplaceParenthesesWithVerticalBars() {
		String actual = JUnitStringDecorator.getJunitSafeString("some string with (parentheses)");
		assertEquals("some string with |parentheses|", actual);
	}

	@Test
	public void shouldReplaceCrLfWithCommas() {
		String actual = JUnitStringDecorator.getJunitSafeString("some\n\r string with \r\n\ncrlf\n\n");
		assertEquals("some,  string with , crlf, ", actual);
	}

	@Test
	public void shouldReplaceDotWithOneDotLeader() {
		String actual = JUnitStringDecorator.getJunitSafeString("some string. with dots.");
		assertEquals("some string\u2024 with dots\u2024", actual);
	}
}
