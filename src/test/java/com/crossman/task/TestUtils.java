package com.crossman.task;

import com.crossman.util.ExceptionList;

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

	public static void assertSameExceptionList(List<Exception> expected, ExceptionList actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}
}
