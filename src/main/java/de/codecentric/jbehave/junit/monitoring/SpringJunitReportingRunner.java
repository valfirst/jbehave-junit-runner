package de.codecentric.jbehave.junit.monitoring;

import org.jbehave.core.ConfigurableEmbedder;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringJUnitReportingRunner extends JUnitReportingRunner {

	public SpringJUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass) throws Throwable {
		super(testClass);
	}

	@Override
	protected void prepareConfigurableEmbedder(Class<? extends ConfigurableEmbedder> testClass, ConfigurableEmbedder configurableEmbedder) throws Exception {
		SpringJunit4ClassRunnerExecutor executor = new SpringJunit4ClassRunnerExecutor(testClass);
		executor.getHiddenTestContextManager().prepareTestInstance(configurableEmbedder);
	}

	private static class SpringJunit4ClassRunnerExecutor extends SpringJUnit4ClassRunner {
		public SpringJunit4ClassRunnerExecutor(Class<?> clazz) throws InitializationError {
			super(clazz);
		}

		public TestContextManager getHiddenTestContextManager() {
			return getTestContextManager();
		}
	}
}