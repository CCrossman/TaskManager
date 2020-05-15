package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.crossman.task.TestUtils.assertSameList;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class TaskFlatMapTestSuite {

	@Test
	public void testFlatMapSS() {
		List<String> result = new ArrayList<>();
		Task<Void> t$1 = SuccessfulTask.instance;
		Task<String> t$2 = t$1.flatMap($ -> Task.constant("Hello World"));
		t$2.forEach((s,e) -> {
			assertNull(e);
			result.add(s);
		});

		assertSameList(Collections.singletonList("Hello World"), result);
	}

	@Test
	public void testFlatMapSF() {
		final IllegalArgumentException ex = new IllegalArgumentException();
		List<Exception> result = new ArrayList<>();
		Task<Void> t$1 = SuccessfulTask.instance;
		Task<String> t$2 = t$1.flatMap($ -> BasicTask.failed(ex));
		t$2.forEach((s,e) -> {
			assertNull(s);
			result.add(e);
		});

		assertSameList(Collections.singletonList(ex), result);
	}

	@Test
	public void testFlatMapSI() {
		List<Object> result = new ArrayList<>();
		Task<Void> t$1 = SuccessfulTask.instance;
		Task<String> t$2 = t$1.flatMap($ -> BasicTask.incomplete());
		t$2.forEach((s,e) -> {
			result.add(s);
			result.add(e);
		});

		assertSameList(Collections.emptyList(), result);
	}

	@Test
	public void testFlatMapF() {
		List<Exception> result = new ArrayList<>();
		Task<Void> t$1 = FailedTask.instance;
		Task<String> t$2 = t$1.flatMap($ -> BasicTask.succeed("hello"));
		t$2.forEach((s,e) -> {
			assertNull(s);
			result.add(e);
		});

		assertSameList(Collections.singletonList(FailedTask.GENERIC_FAILURE), result);
	}

	@Test
	public void testFlatMapI() {
		List<Object> result = new ArrayList<>();
		Task<Void> t$1 = IncompleteTask.instance;
		Task<String> t$2 = t$1.flatMap($ -> BasicTask.succeed("hello"));
		t$2.forEach((s,e) -> {
			result.add(s);
			result.add(e);
		});

		assertSameList(Collections.emptyList(), result);
	}
}
