package org.jbehave.scenario.finegrained.junit.monitoring;

import java.util.HashSet;
import java.util.Set;


//hello
public class DescriptionTextUniquefier {

    Set<String> strings = new HashSet<String>();
    
    public String getUniqueDescription(String junitSafeString) {
	while(strings.contains(junitSafeString)) {
	    junitSafeString = junitSafeString + ".";
	}
	strings.add(junitSafeString);
	return junitSafeString;
    }

}
//wsdcfkhvbqweflhvqwefhvbqwefjhvqwfghjk