package de.codecentric.jbehave.junit.monitoring;

import org.jbehave.core.ConfigurableEmbedder;

public class SpringJUnitReportingRunner extends JUnitReportingRunner {

	public SpringJUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass) throws Throwable {
		super(testClass);
	}

	@Override
	protected void prepareConfigurableEmbedder(Class<? extends ConfigurableEmbedder> testClass, ConfigurableEmbedder configurableEmbedder) throws Exception {
		SpringJunit4ClassRunnerExecutor executor = new SpringJunit4ClassRunnerExecutor(testClass);
		executor.getHiddenTestContextManager().prepareTestInstance(configurableEmbedder);
	}
}