package com.github.TurquoiseSpace.jbehave.junit.monitoring;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;

/**
 * @author Valery Yatsynovich
 */
public class StoryPathsExtractor {

	private final ConfigurableEmbedder configurableEmbedder;

	public StoryPathsExtractor(ConfigurableEmbedder configurableEmbedder) {
		this.configurableEmbedder = configurableEmbedder;
	}

	public List<String> getStoryPaths() throws ReflectiveOperationException {
		if (configurableEmbedder instanceof JUnitStories) {
			return extractStoryPathsFromJUnitStories();
		} else if (configurableEmbedder instanceof JUnitStory) {
			return extractStoryPathsFromJUnitStory();
		}
		throw new IllegalArgumentException(
				"Only ConfigurableEmbedder of types JUnitStory and JUnitStories is supported");
	}

	@SuppressWarnings("unchecked")
	private List<String> extractStoryPathsFromJUnitStories() throws ReflectiveOperationException {
		Method method = makeStoryPathsMethodPublic((Class<? extends JUnitStories>) configurableEmbedder.getClass());
		return ((List<String>) method.invoke(configurableEmbedder, (Object[]) null));
	}

	@SuppressWarnings("unchecked")
	private static Method makeStoryPathsMethodPublic(Class<? extends JUnitStories> clazz) throws NoSuchMethodException {
		try {
			Method method = clazz.getDeclaredMethod("storyPaths", (Class[]) null);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null && JUnitStories.class.isAssignableFrom(superclass)) {
				return makeStoryPathsMethodPublic((Class<? extends JUnitStories>) superclass);
			}
			throw e;
		}
	}

	private List<String> extractStoryPathsFromJUnitStory() {
		StoryPathResolver storyPathResolver = configurableEmbedder.configuredEmbedder().configuration()
				.storyPathResolver();
		return Collections.singletonList(storyPathResolver.resolve(configurableEmbedder.getClass()));
	}
}
