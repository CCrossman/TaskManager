package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static com.crossman.task.TestUtils.assertSameList;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BasicTaskTestSuite {

	@Test
	public void testTaskConstructor() {
		Task task = new BasicTask<>();
		assertNotNull(task);
	}

	@Test
	public void testTaskIsNotCompletedWhenConstructed() {
		Task task = new BasicTask<>();
		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());
	}

	@Test
	public void testTaskIsNotSucceedingWhenNotCompleted() {
		Task task = new BasicTask<>();
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());
	}

	@Test
	public void testTaskIsNotFailingWhenNotCompleted() {
		Task task = new BasicTask<>();
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isFailure());
	}

	@Test
	public void testTaskCanCompleteSuccessfully() {
		List<String> results = new ArrayList<>();

		BasicTask<String> task = new BasicTask<>();
		task.forEach((s,e) -> results.add(s));

		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());
		assertSameList(Collections.emptyList(), results);

		task.completeSuccessfully("foo");

		assertTrue(task.isCompleted());
		assertFalse(task.isNotCompleted());
		assertEquals(Optional.of(true), task.isSuccess());
		assertEquals(Optional.of(false), task.isFailure());
		assertSameList(Collections.singletonList("foo"), results);
	}

	@Test
	public void testTaskCanCompleteUnsuccessfully() {
		final Exception exception = new Exception();
		List<Exception> results = new ArrayList<>();

		BasicTask<String> task = new BasicTask<>();
		task.forEach((o,e) -> results.add(e));

		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());
		assertSameList(Collections.emptyList(), results);

		task.completeUnsuccessfully(exception);

		assertTrue(task.isCompleted());
		assertFalse(task.isNotCompleted());
		assertEquals(Optional.of(false), task.isSuccess());
		assertEquals(Optional.of(true), task.isFailure());
		assertSameList(Collections.singletonList(exception), results);
	}
}
