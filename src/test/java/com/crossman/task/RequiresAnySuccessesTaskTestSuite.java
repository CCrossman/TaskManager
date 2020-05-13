package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RequiresAnySuccessesTaskTestSuite {

	@Test
	public void testConstructor() {
		Task task = GradedTask.requiresAnySuccesses(Task.success, Task.failure);
		assertNotNull(task);
	}

	@Test
	public void testSucceedsIfAllSucceed() {
		Task task = GradedTask.requiresAnySuccesses(Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());
	}

	@Test
	public void testSucceedsIfOneSucceed() {
		Task task = GradedTask.requiresAnySuccesses(Task.success, Task.failure);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());
	}

	@Test
	public void testFailsIfAllFailed() {
		Task task = GradedTask.requiresAnySuccesses(Task.failure, Task.failure);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(false), task.isSuccess());
	}

	@Test
	public void testCompletesIfAllCompleted() {
		Task task = GradedTask.requiresAnySuccesses(Task.success, Task.failure, Task.incomplete);
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());
	}

	@Test
	public void testEmptyConstructor() {
		Task task = GradedTask.requiresAnySuccesses();
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(false), task.isSuccess());
	}
}
