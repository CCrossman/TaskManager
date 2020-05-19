package com.crossman.task;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class TestUtils {
	private TestUtils() {}

	public static <T> void assertSameList(List<T> expectedList, List<T> actualList) {
		assertEquals(expectedList.size(), actualList.size());
		for (int i = 0; i < expectedList.size(); ++i) {
			assertEquals(expectedList.get(i), actualList.get(i));
		}
	}
}
