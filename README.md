JBehave JUnit Integration
=========================
[![Build Status](https://travis-ci.org/valfirst/jbehave-junit-runner.svg?branch=master)](https://travis-ci.org/valfirst/jbehave-junit-runner)
[![Coverage Status](https://coveralls.io/repos/github/valfirst/jbehave-junit-runner/badge.svg?branch=master)](https://coveralls.io/github/valfirst/jbehave-junit-runner?branch=master)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.github.valfirst:jbehave-junit-runner)](https://sonarcloud.io/dashboard/index/com.github.valfirst:jbehave-junit-runner)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d81f58136aa245668240b7d851a54d50)](https://www.codacy.com/app/valfirst/jbehave-junit-runner?utm_source=github.com&utm_medium=referral&utm_content=valfirst/jbehave-junit-runner&utm_campaign=badger)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.valfirst/jbehave-junit-runner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.valfirst/jbehave-junit-runner/)
[![Javadocs](http://www.javadoc.io/badge/com.github.valfirst/jbehave-junit-runner.svg)](http://www.javadoc.io/doc/com.github.valfirst/jbehave-junit-runner)
[![Dependency Status](https://beta.gemnasium.com/badges/github.com/valfirst/jbehave-junit-runner.svg)](https://beta.gemnasium.com/projects/github.com/valfirst/jbehave-junit-runner)
[![Known Vulnerabilities](https://snyk.io/test/github/valfirst/jbehave-junit-runner/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/valfirst/jbehave-junit-runner?targetFile=pom.xml)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/valfirst/jbehave-junit-runner/master/LICENSE.txt)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fvalfirst%2Fjbehave-junit-runner.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fvalfirst%2Fjbehave-junit-runner?ref=badge_shield)


This little project is designed to make JBehave
stories & scenarios show up in the JUnit view
in Eclipse and potentially other IDEs that support
custom test runners.

Installation
==========================
The project consists of only a handful of classes
and is easy to use.


Using Maven
-------------------
The easiest way is to include
it in your acceptance test project(s) by ways of
a Maven dependency. Binary, Sources and Javadocs are
all available from Maven Central.

```xml
<dependency>
    <groupId>com.github.valfirst</groupId>
    <artifactId>jbehave-junit-runner</artifactId>
    <version>2.1.2</version>
</dependency>
```

Note that the JBehave library is set to scope `provided`
in the POM, because any project using this Runner will
naturally have their own version of JBehave already.

Current development SNAPSHOT versions are available from
[Sonatype's Nexus for Open Source projects](https://oss.sonatype.org/content/repositories/snapshots/com/github/valfirst/jbehave-junit-runner/).

Without Maven
---------------------
If you cannot or do not want to use Maven, just
add the library to your classpath.

Also make sure you add the dependencies of this library to the classpath as well. They can be
seen in the `pom.xml`. For convenience, they are listed here, too:

  * jbehave-core (4.1+)
  * junit (4.12)

Your project will of course have JBehave already, just make sure it is version 4.1 or above.


Usage
====================================
Once it is on your classpath, the runner can be used
to visualize the structure and the successes/failures
of your JBehave acceptance tests.

Enabling
-------------------------------
You will probably already have one or more classes
that extend the default JBehave `JUnitStories` class.
Just add this annotation:

```java
@RunWith(JUnitReportingRunner.class)
public class MyAcceptanceTests extends JUnitStories {
    ...
}
```

Usually, this is everything you have to do. If you want to,
you can use a little helper method to configure the configured
Embedder so that it works nicely together with the JUnitReportingRunner.

```java
JUnitReportingRunner.recommendedControls(configuredEmbedder());
```

This tells JBehave to use only one thread, and don't throw any exceptions
when generating views or otherwise wrapping the execution up. This would
confuse JUnit considerably, rendering the AfterStories Suite incomplete.

Caveats
----------------------------
There are a few issues you might encounter upon first installation and
integration into your project. To alleviate these, the following are a
few common pitfalls.

### java.lang.NoSuchMethodException: ...storyPaths()

If you run into problems like this

```java
java.lang.NoSuchMethodException: MyAcceptanceTests.storyPaths()
    at java.lang.Class.getDeclaredMethod(Class.java:1937)
    at org.jbehave.scenario.finegrained.junit.monitoring.JUnitReportingRunner.<init>(JUnitReportingRunner.java:33)
    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
    at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
    at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
    at org.junit.internal.builders.AnnotatedBuilder.buildRunner(AnnotatedBuilder.java:31)
    ....
```	

you are likely extending a custom base class which
was probably introduced to provide some common methods
for all  your test classes. This is perfectly fine,
however in case you pulled up the `storyPaths()` method
into that common base class, you

  1. either need to make it public _OR_
  1. override it in all descendants of that base class
     (just calling the super implementation is fine).

This is due to a reflection issue and might be fixed in
future versions.


### java.lang.NoSuchMethodError: org.junit.runner.Description.createSuiteDescription

If you see JUnit throw an error soon after a test starts, that looks like this

```java
java.lang.NoSuchMethodError: org.junit.runner.Description.createSuiteDescription(Ljava/lang/String;[Ljava/lang/annotation/Annotation;)Lorg/junit/runner/Description;
    at de.codecentric.jbehave.junit.monitoring.JUnitDescriptionGenerator.createDescriptionFrom(JUnitDescriptionGenerator.java:43)
    at de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner.buildDescriptionFromStories(JUnitReportingRunner.java:152)
    at de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner.<init>(JUnitReportingRunner.java:73)
    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
    ...
```

you most probably have an old version of JUnit on your classpath that does not have
that method signature yet. Verify that you are using a recent version of
JUnit and that it is not hidden by an older version which might hide it by being
declared first on the classpath.

Example output
=================================
Once you are all set, you use the regular `Run As -> JUnit Test`
command in Eclipse. The JUnit view should appear and display something
like this:

![Screenshot](https://raw.githubusercontent.com/codecentric/jbehave-junit-runner/master/doc/img/JBehaveJUnitScreenshot.png)


Reporting Issues
=================================
In case you run into problems or have ideas on how to improve this
piece of software, please do not hesitate to open an issue in the
issue tracker, here on GitHub.

To make things easier (and quicker) for yourself (and us), please
provide the following pieces of information in your ticket:

  * The version you are using (e. g. "1.0.0" or "1.0.1-SNAPSHOT as of 2012-05-25")
  * If possible, a stack trace of the error.
  * Ideally, a limited test case that allows us to reproduce the
    problem locally. This is indeed the best way to get a bug fixed
    quickly, because we'll have something specific to work on.
  * Any information about your environment that might be helpful, e. g.
    your JBehave version, which operating system, etc.

It might also be a good idea to try, if your particular problem has
been fixed already in the latest snapshot release.

And, *of course*, we'd also very much appreciate pull requests for fixes
that you have already made yourself. After all, this is where open source
shines most :)

Credits
=================================
This project is based on the original implementation by Mark Burnett, located at http://code.google.com/p/jbehave-junit-monitor/ which worked with earlier versions of JBehave.

## spring support
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.valfirst.jbehave.junit.monitoring.JUnitDescriptionGenerator;
import com.github.valfirst.jbehave.junit.monitoring.JUnitScenarioReporter;

public class SpringReportingRunner extends SpringJUnit4ClassRunner{


	private Embedder configuredEmbedder;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	private ConfigurableEmbedder configurableEmbedder;

	public SpringReportingRunner(Class<? extends ConfigurableEmbedder> testClass)
			throws InitializationError, ReflectiveOperationException {
		super(testClass);
		super.getTestContextManager();
		try {
			configurableEmbedder =(ConfigurableEmbedder) super.createTest();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
		configuration = configuredEmbedder.configuration();

		List<String> storyPaths = getStoryPaths(testClass);

		StepMonitor originalStepMonitor = configuration.stepMonitor();
		configuration.useStepMonitor(new NullStepMonitor());
		List<Description> storyDescriptions = buildDescriptionFromStories(storyPaths);
		configuration.useStepMonitor(originalStepMonitor);

		rootDescription = Description.createSuiteDescription(testClass);
		for (Description storyDescription : storyDescriptions) {
			rootDescription.addChild(storyDescription);
		}
	}

	@Override
	public Description getDescription() {
		return rootDescription;
	}

	@Override
	public int testCount() {
		return numberOfTestCases;
	}

	/**
	 * Returns a {@link Statement}: Call {@link #runChild(Object, RunNotifier)}
	 * on each object returned by {@link #getChildren()} (subject to any imposed
	 * filter and sort)
	 */
	@Override
	protected Statement childrenInvoker(final RunNotifier notifier) {
		return new Statement() {
			@Override
			public void evaluate() {
				JUnitScenarioReporter junitReporter = new JUnitScenarioReporter(
				notifier, numberOfTestCases, rootDescription, configuration.keywords());
				// tell the reporter how to handle pending steps
				junitReporter.usePendingStepStrategy(configuration
						.pendingStepStrategy());

				addToStoryReporterFormats(junitReporter);

				try {
					configurableEmbedder.run();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static EmbedderControls recommendedControls(Embedder embedder) {
		return embedder.embedderControls()
		// don't throw an exception on generating reports for failing stories
				.doIgnoreFailureInView(true)
				// don't throw an exception when a story failed
				.doIgnoreFailureInStories(true)
				// .doVerboseFailures(true)
				.useThreads(1);
	}

	private List<String> getStoryPaths(Class<? extends ConfigurableEmbedder> testClass)
			throws ReflectiveOperationException {
		if (JUnitStories.class.isAssignableFrom(testClass)) {
			return getStoryPathsFromJUnitStories(testClass);
		} else if (JUnitStory.class.isAssignableFrom(testClass)) {
			return Arrays.asList(configuration.storyPathResolver().resolve(testClass));
		} else {
			throw new IllegalArgumentException(
					"Only ConfigurableEmbedder of types JUnitStory and JUnitStories is supported");
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> getStoryPathsFromJUnitStories(
			Class<? extends ConfigurableEmbedder> testClass)
			throws ReflectiveOperationException {
		Method method = makeStoryPathsMethodPublic(testClass);
		return ((List<String>) method.invoke(configurableEmbedder, (Object[]) null));
	}

	@SuppressWarnings("unchecked")
    private static Method makeStoryPathsMethodPublic(Class<? extends ConfigurableEmbedder> clazz)
			throws NoSuchMethodException {
		try {
			Method method = clazz.getDeclaredMethod("storyPaths", (Class[]) null);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null && ConfigurableEmbedder.class.isAssignableFrom(superclass)) {
				return makeStoryPathsMethodPublic((Class<? extends ConfigurableEmbedder>) superclass);
			}
			throw e;
		}
	}

	private List<CandidateSteps> getCandidateSteps() {
		List<CandidateSteps> candidateSteps;
		InjectableStepsFactory stepsFactory = configurableEmbedder.stepsFactory();
		if (stepsFactory != null) {
			candidateSteps = stepsFactory.createCandidateSteps();
		} else {
			candidateSteps = configuredEmbedder.candidateSteps();
			if (candidateSteps == null || candidateSteps.isEmpty()) {
				candidateSteps = configuredEmbedder.stepsFactory().createCandidateSteps();
			}
		}
		return candidateSteps;
	}

	private void addToStoryReporterFormats(JUnitScenarioReporter junitReporter) {
		StoryReporterBuilder storyReporterBuilder = configuration
				.storyReporterBuilder();
		StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(
				junitReporter);
		storyReporterBuilder.withFormats(junitReportFormat);
	}

	private List<Description> buildDescriptionFromStories(List<String> storyPaths) {
		JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(
				getCandidateSteps(), configuration);
		List<Description> storyDescriptions = new ArrayList<>();

		addSuite(storyDescriptions, "BeforeStories");
		storyDescriptions.addAll(descriptionGenerator.createDescriptionFrom(createPerformableTree(storyPaths)));
		addSuite(storyDescriptions, "AfterStories");

		numberOfTestCases += descriptionGenerator.getTestCases();

		return storyDescriptions;
	}

	private PerformableTree createPerformableTree(List<String> storyPaths) {
		BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
		PerformableTree performableTree = new PerformableTree();
		RunContext context = performableTree.newRunContext(configuration, configuredEmbedder.stepsFactory(),
				configuredEmbedder.embedderMonitor(), configuredEmbedder.metaFilter(), failures);
		performableTree.addStories(context, storiesOf(performableTree, storyPaths));
		return performableTree;
	}

	private List<Story> storiesOf(PerformableTree performableTree, List<String> storyPaths) {
		List<Story> stories = new ArrayList<>();
		for (String storyPath : storyPaths) {
			stories.add(performableTree.storyOfPath(configuration, storyPath));
		}
		return stories;
	}

	private void addSuite(List<Description> storyDescriptions, String name) {
		storyDescriptions.add(Description.createTestDescription(Object.class,
				name));
		numberOfTestCases++;
	}
}

### springsupport useage
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringReportingRunner.class)
@SpringBootTest(properties = { "zookeeper.address=192.168.1.14:12181", "application.host=127.0.0.1",
		"application.port=90", "profile=dev" })
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "dev")
public class Test2 extends JUnitStories {
	@Autowired
	protected ApplicationContext context;
	@Override
	public InjectableStepsFactory stepsFactory() {
		if (super.hasStepsFactory()) {
			return super.stepsFactory();
		}
		return new SpringStepsFactory(configuration(), context);
	}

	@Override
	public Configuration configuration() {
		if (super.hasConfiguration()) {
			return super.configuration();
		}
		Configuration configuration = new MostUsefulConfiguration().useStoryReporterBuilder(new StoryReporterBuilder()
				.withDefaultFormats().withFormats(Format.CONSOLE, Format.TXT, Format.HTML, Format.XML)
				.withCrossReference(new CrossReference()).withFailureTrace(true));
		return configuration;
	}

	@Override
	protected List<String> storyPaths() {
		List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromPath("src/test/resources"), "**/*.story",
				"");
		return storyPaths;
	}

	


}

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fvalfirst%2Fjbehave-junit-runner.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fvalfirst%2Fjbehave-junit-runner?ref=badge_large)
