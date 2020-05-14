package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static com.crossman.task.TestUtils.assertSameList;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RequiresSomeSuccessesTaskTestSuite {

	@Test
	public void testConstructor() {
		Task<Collection<Void>> task = GradedTask.requiresSomeSuccesses(1, Task.success, Task.failure);
		assertNotNull(task);
	}

	@Test
	public void testSucceedsIfAtLeastNSucceed() {
		List<Void> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresSomeSuccesses(2, Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull(e);
			results.addAll($);
		});

		assertSameList(Arrays.asList(null,null), results);
	}

	@Test
	public void testFailsIfAtLeastNDontSucceed() {
		List<Void> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresSomeSuccesses(3, Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(false), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull(e);
			results.addAll($);
		});

		assertSameList(Arrays.asList(null,null), results);
	}

	@Test
	public void testCompletesIfAllCompleted() {
		List<Object> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresSomeSuccesses(1, Task.success, Task.failure, Task.incomplete);
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());

		task.forEach(($,e) -> {
			results.addAll($);
			results.add(e);
		});

		assertTrue(results.isEmpty());
	}

	@Test
	public void testEmptyConstructor() {
		List<Void> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresSomeSuccesses(0);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull(e);
			results.addAll($);
		});

		assertTrue(results.isEmpty());
	}
}
