package com.crossman.task;

import com.crossman.util.ExceptionList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static com.crossman.task.TestUtils.assertSameExceptionList;
import static com.crossman.task.TestUtils.assertSameList;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RequiresAnySuccessesTaskTestSuite {

	@Test
	public void testConstructor() {
		List<Exception> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresAnySuccesses(Task.success, Task.failure);
		assertNotNull(task);

		task.forEach(($,e) -> {
			assertNull($);
			results.add(e);
		});

		assertEquals(1, results.size());
		assertSameExceptionList(Collections.singletonList(FailedTask.GENERIC_FAILURE), (ExceptionList)results.get(0));
	}

	@Test
	public void testSucceedsIfAllSucceed() {
		List<Void> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresAnySuccesses(Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull(e);
			results.addAll($);
		});

		assertSameList(Arrays.asList(null,null), results);
	}

	@Test
	public void testSucceedsIfAnySucceeded() {
		List<Exception> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresAnySuccesses(Task.success, Task.failure);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull($);
			results.add(e);
		});

		assertEquals(1, results.size());
		assertSameExceptionList(Collections.singletonList(FailedTask.GENERIC_FAILURE), (ExceptionList)results.get(0));
	}

	@Test
	public void testCompletesIfAllCompleted() {
		List<Object> results = new ArrayList<>();
		Task<Collection<Void>> task = GradedTask.requiresAnySuccesses(Task.success, Task.failure, Task.incomplete);
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
		Task<Collection<Void>> task = GradedTask.requiresAnySuccesses();
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(false), task.isSuccess());

		task.forEach(($,e) -> {
			assertNull(e);
			results.addAll($);
		});

		assertTrue(results.isEmpty());
	}
}
