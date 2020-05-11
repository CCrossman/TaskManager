package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RequiresSomeSuccessesTaskTestSuite {

	@Test
	public void testConstructor() {
		Task task = RequiresSomeSuccesses.of(1, Task.success, Task.failure);
		assertNotNull(task);
	}

	@Test
	public void testSucceedsIfAtLeastNSucceed() {
		Task task = RequiresSomeSuccesses.of(2, Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(true), task.isSuccess());
	}

	@Test
	public void testFailsIfAtLeastNDontSucceed() {
		Task task = RequiresSomeSuccesses.of(3, Task.success, Task.success);
		assertTrue(task.isCompleted());
		assertEquals(Optional.of(false), task.isSuccess());
	}

	@Test
	public void testCompletesIfAllCompleted() {
		Task task = RequiresSomeSuccesses.of(1, Task.success, Task.failure, Task.incomplete);
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());
	}

	@Test
	public void testEmptyConstructor() {
		Task task = RequiresSomeSuccesses.of(0);
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());
	}
}
