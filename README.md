JBehave JUnit Integration
=========================

[![jbehave-junit-runner CI](https://github.com/TurquoiseSpace/jbehave-junit-runner/actions/workflows/ci.yml/badge.svg)](https://github.com/TurquoiseSpace/jbehave-junit-runner/actions/workflows/ci.yml)

[![Coverage Status](https://coveralls.io/repos/github/TurquoiseSpace/jbehave-junit-runner/badge.svg?branch=master)](https://coveralls.io/github/TurquoiseSpace/jbehave-junit-runner?branch=master)

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=TurquoiseSpace_jbehave-junit-runner&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TurquoiseSpace_jbehave-junit-runner)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d81f58136aa245668240b7d851a54d50)](https://www.codacy.com/gh/TurquoiseSpace/jbehave-junit-runner/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TurquoiseSpace/jbehave-junit-runner&amp;utm_campaign=Badge_Grade)

[![Maven Central](https://img.shields.io/maven-central/v/com.github.TurquoiseSpace/jbehave-junit-runner.svg)](https://central.sonatype.com/search?q=jbehave-junit-runner&namespace=com.github.TurquoiseSpace)
[![Javadocs](http://www.javadoc.io/badge/com.github.TurquoiseSpace/jbehave-junit-runner.svg)](http://www.javadoc.io/doc/com.github.TurquoiseSpace/jbehave-junit-runner)

[![Known Vulnerabilities](https://snyk.io/test/github/TurquoiseSpace/jbehave-junit-runner/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/TurquoiseSpace/jbehave-junit-runner?targetFile=pom.xml)

[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/TurquoiseSpace/jbehave-junit-runner/master/LICENSE)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner.svg?type=shield&issueType=license)](https://app.fossa.com/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner?ref=badge_shield&issueType=license)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner.svg?type=small)](https://app.fossa.com/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner?ref=badge_small)


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
    <groupId>com.github.TurquoiseSpace</groupId>
    <artifactId>jbehave-junit-runner</artifactId>
    <version>2.3.3</version>
</dependency>
```

Note that the JBehave library is set to scope `provided`
in the POM, because any project using this Runner will
naturally have their own version of JBehave already.

Current development SNAPSHOT versions are available from
[Sonatype's Nexus for Open Source projects](https://oss.sonatype.org/content/repositories/snapshots/com/github/TurquoiseSpace/jbehave-junit-runner/).

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


## License

MIT License

[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/TurquoiseSpace/jbehave-junit-runner/master/LICENSE)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner?ref=badge_shield)

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner?ref=badge_large)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner.svg?type=large&issueType=license)](https://app.fossa.com/projects/git%2Bgithub.com%2FTurquoiseSpace%2Fjbehave-junit-runner?ref=badge_large&issueType=license)
