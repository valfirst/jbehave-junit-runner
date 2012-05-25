JBehave JUnit Integration
=========================

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

    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>jbehave-junit-runner</artifactId>
        <version>1.0.0</version>
    </dependency>

Note that the JBehave library is set to scope `provided`
in the POM, because any project using this Runner will
naturally have their own version of JBehave already.

Current development SNAPSHOT versions are available from 
[Sonatype's Nexus for Open Source projects](https://oss.sonatype.org/content/repositories/snapshots/de/codecentric/jbehave-junit-runner/).

Without Maven
---------------------
If you cannot or do not want to use Maven, just
add the library to your classpath.

Also make sure you add the dependencies of this
library to the classpath as well. They can be
seen in the `pom.xml`. For convenience, they are
listed here, too:

  * jbehave-core (3.6+)
  * junit (4.10)
  * slf4j-api (1.6.4)

Your project will of course have JBehave already,
just make sure it is version 3.6 or above.


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

    @RunWith(JUnitReportingRunner.class)
    public class MyAcceptanceTests extends JUnitStories {
        ....
    }

Usually, this is everything you have to do.

Caveat
----------------------------
If you run into problems like this

    java.lang.NoSuchMethodException: MyAcceptanceTests.storyPaths()
        at java.lang.Class.getDeclaredMethod(Class.java:1937)
        at org.jbehave.scenario.finegrained.junit.monitoring.JUnitReportingRunner.<init>(JUnitReportingRunner.java:33)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.junit.internal.builders.AnnotatedBuilder.buildRunner(AnnotatedBuilder.java:31)
        ....
        
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


Example output
=================================
Once you are all set, you use the regular `Run As -> JUnit Test`
command in Eclipse. The JUnit view should appear and display something
like this:

![Screenshot](/dschneller/jbehave-junit-monitor/raw/master/doc/img/JBehaveJUnitScreenshot.png)


Reporting Issues
=================================
In case you run into problems or have ideas on how to improve this
piece of software, please do not hesitate to open an issue in the
issue tracker, here on GitHub.

To make things easier (and quicker) for yourself (and us), please 
provide the following pieces of information in your ticket:

  * The version you are using (e. g. 1.0.0 or 1.0.1-SNAPSHOT as of 2012-05-25)
  * If possible, a stack trace of the error.
  * Ideally, a limited test case that allows us to reproduce the 
    problem locally. This is indeed the best way to get a bug fixed
    quickly, because we'll have something specific to work on.
  * Any information about your environment that might be helpful, e. g.
    your JBehave version, which operating system, etc.

It might also be a good idea to try, if your particular problem might have
been fixed already in the latest snapshot release.

And, *of course*, we'd also very much appreciate it pull requests for fixes
that you have already made yourself. After all, this is where open source
shines most :)

Credits
=================================
This project is based on the original implementation by Mark Burnett, located at [http://code.google.com/p/jbehave-junit-monitor/] which worked with earlier versions of JBehave.
