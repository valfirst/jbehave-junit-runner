package de.codecentric.jbehave.junit.monitoring;

import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.DEBUG;
import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.INFO;
import static de.codecentric.jbehave.junit.monitoring.Logger.LogLevel.NONE;

import java.io.PrintStream;

public class Logger {
	public static final String PROP_JJM_LOGLEVEL = "jjm.loglevel";
	private volatile LogLevel logLevel;
	PrintStream logStream = System.out;

	public void info(String message, Object... params) {
		if (!isInfoEnabled()) {
			return;
		}
		printMessage(INFO, message, params);
	}

	public void debug(String message, Object... params) {
		if (!isDebugEnabled()) {
			return;
		}
		printMessage(DEBUG, message, params);
	}

	boolean isInfoEnabled() {
		return isLevelEnabled(INFO);
	}

	boolean isLevelEnabled(LogLevel level) {
		return getLogLevel().ordinal() >= level.ordinal();
	}

	private LogLevel getLogLevel() {
		if (logLevel != null) {
			return logLevel;
		}
		synchronized (Logger.class) {
			if (logLevel == null) {
				String configuredLevel = System.getProperty(PROP_JJM_LOGLEVEL);
				if (configuredLevel == null
						|| "".equals(configuredLevel.trim())) {
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

	boolean isDebugEnabled() {
		return isLevelEnabled(DEBUG);
	}

	private void printMessage(LogLevel level, String message, Object... params) {
		String format = message.replace("{}", "%s");
		Object[] strings = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				strings[i] = "null";
			} else {
				strings[i] = params[i].toString();
			}
		}
		logStream.println(level + ": " + String.format(format, strings));
	}

	enum LogLevel {
		NONE, ERROR, WARN, INFO, DEBUG;
	}
}
