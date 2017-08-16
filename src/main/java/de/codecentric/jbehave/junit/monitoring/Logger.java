package de.codecentric.jbehave.junit.monitoring;

import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.DEBUG;
import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.INFO;
import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.NONE;

import java.io.PrintStream;

import org.apache.commons.lang3.StringUtils;

public class Logger {
	public static final String PROP_JJM_LOGLEVEL = "jjm.loglevel";
	private volatile LogLevel logLevel;
	private PrintStream logStream = System.out;

	public void info(String message, Object... params) {
		printMessage(INFO, message, params);
	}

	public void debug(String message, Object... params) {
		printMessage(DEBUG, message, params);
	}

	private boolean isLevelEnabled(LogLevel level) {
		return getLogLevel().ordinal() >= level.ordinal();
	}

	private LogLevel getLogLevel() {
		if (logLevel != null) {
			return logLevel;
		}
		synchronized (Logger.class) {
			if (logLevel == null) {
				String configuredLevel = System.getProperty(PROP_JJM_LOGLEVEL);
				if (StringUtils.isBlank(configuredLevel)) {
					logLevel = NONE;
				} else {
					configuredLevel = configuredLevel.trim();
					try {
						logLevel = LogLevel.valueOf(configuredLevel
								.toUpperCase());
					} catch (IllegalArgumentException e) {
						logLevel = NONE;
					}
				}
			}
		}
		return logLevel;
	}

	private void printMessage(LogLevel level, String message, Object... params) {
		if (isLevelEnabled(level)) {
			String format = message.replace("{}", "%s");
			Object[] strings = new String[params.length];
			for (int i = 0; i < params.length; i++) {
				strings[i] = String.valueOf(params[i]);
			}
			logStream.println(level + ": " + String.format(format, strings));
		}
	}

	enum LogLevel {
		NONE, ERROR, WARN, INFO, DEBUG
	}
}
