package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BasicTaskTestSuite {

	@Test
	public void testTaskConstructor() {
		Task task = new BasicTask();
		assertNotNull(task);
	}

	@Test
	public void testTaskIsNotCompletedWhenConstructed() {
		Task task = new BasicTask();
		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());
	}

	@Test
	public void testTaskIsNotSucceedingWhenNotCompleted() {
		Task task = new BasicTask();
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isSuccess());
	}

	@Test
	public void testTaskIsNotFailingWhenNotCompleted() {
		Task task = new BasicTask();
		assertFalse(task.isCompleted());
		assertEquals(Optional.empty(), task.isFailure());
	}

	@Test
	public void testTaskCanCompleteSuccessfully() {
		BasicTask task = new BasicTask();
		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());

		task.completeSuccessfully();

		assertTrue(task.isCompleted());
		assertFalse(task.isNotCompleted());
		assertEquals(Optional.of(true), task.isSuccess());
		assertEquals(Optional.of(false), task.isFailure());
	}

	@Test
	public void testTaskCanCompleteUnsuccessfully() {
		BasicTask task = new BasicTask();
		assertFalse(task.isCompleted());
		assertTrue(task.isNotCompleted());

		task.completeUnsuccessfully();

		assertTrue(task.isCompleted());
		assertFalse(task.isNotCompleted());
		assertEquals(Optional.of(false), task.isSuccess());
		assertEquals(Optional.of(true), task.isFailure());
	}
}
