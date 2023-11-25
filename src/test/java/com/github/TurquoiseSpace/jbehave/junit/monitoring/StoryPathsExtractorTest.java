package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.junit.Test;

public class StoryPathsExtractorTest {

	private static final List<String> JUNIT_STORIES_PATHS = Arrays.asList("/path/story1.story", "/path/story2.story");

	@Test
	public void shouldExtractStoryPathsFromJUnitStory() throws ReflectiveOperationException {
		TestJUnitStory testJUnitStory = new TestJUnitStory();
		Embedder embedder = mock(Embedder.class);
		testJUnitStory.useEmbedder(embedder);
		Configuration configuration = mock(Configuration.class);
		when(embedder.configuration()).thenReturn(configuration);
		StoryPathResolver storyPathResolver = mock(StoryPathResolver.class);
		when(configuration.storyPathResolver()).thenReturn(storyPathResolver);
		String storyPath = "/path/story.story";
		when(storyPathResolver.resolve(TestJUnitStory.class)).thenReturn(storyPath);
		assertEquals(Collections.singletonList(storyPath), new StoryPathsExtractor(testJUnitStory).getStoryPaths());
	}

	@Test
	public void shouldExtractStoryPathsFromJUnitStories() throws ReflectiveOperationException {
		assertEquals(JUNIT_STORIES_PATHS, new StoryPathsExtractor(new TestJUnitStories()).getStoryPaths());
	}

	@Test
	public void shouldExtractStoryPathsFromJUnitStoriesChild() throws ReflectiveOperationException {
		assertEquals(JUNIT_STORIES_PATHS, new StoryPathsExtractor(new ChildTestJUnitStories()).getStoryPaths());
	}

	@Test
	public void shouldThrowExceptionWhenTypeOfConfigurableEmbedderIsUnknown() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new StoryPathsExtractor(new TestConfigurableEmbedder()).getStoryPaths());
		assertEquals("Only ConfigurableEmbedder of types JUnitStory and JUnitStories is supported",
				exception.getMessage());
	}

	public static class TestJUnitStory extends JUnitStory {
	}

	public static class TestJUnitStories extends JUnitStories {
		@Override
		protected List<String> storyPaths() {
			return JUNIT_STORIES_PATHS;
		}
	}

	public static class ChildTestJUnitStories extends TestJUnitStories {
	}

	public static class TestConfigurableEmbedder extends ConfigurableEmbedder {
		@Override
		@Test
		public void run() {
			fail("Should not run");
		}
	}
}
