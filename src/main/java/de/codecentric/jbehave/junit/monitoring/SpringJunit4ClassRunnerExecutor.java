package de.codecentric.jbehave.junit.monitoring;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringJunit4ClassRunnerExecutor extends SpringJUnit4ClassRunner {
    public SpringJunit4ClassRunnerExecutor(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    public TestContextManager getHiddenTestContextManager() {
        return getTestContextManager();
    }

}
