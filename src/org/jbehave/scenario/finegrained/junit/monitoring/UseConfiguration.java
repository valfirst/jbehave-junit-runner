package org.jbehave.scenario.finegrained.junit.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.scenario.Configuration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseConfiguration {

	Class<? extends Configuration> value();

}
