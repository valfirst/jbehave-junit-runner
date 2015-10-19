package de.codecentric.jbehave.junit.monitoring;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Valery_Yatsynovich
 */
public class JUnitStringDecoratorTest
{
	@Test
	public void shouldReplaceParenthesesWithVerticalBars() {
		String actual = JUnitStringDecorator.getJunitSafeString("some string with (parentheses)");
		Assert.assertEquals("some string with |parentheses|", actual);
	}

	@Test
	public void shouldReplaceCrLfWithCommas() {
		String actual = JUnitStringDecorator.getJunitSafeString("some\n\r string with \r\n\ncrlf\n\n");
		Assert.assertEquals("some,  string with , crlf, ", actual);
	}
}
