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
public class TaskMapTestSuite {

	@Test
	public void testBasicTaskMap() {
		List<Integer> result = new ArrayList<>();
		BasicTask<String> t$1 = new BasicTask<>();
		Task<Integer> t$2 = t$1.map(String::length);
		t$2.forEach((i,e) -> result.add(i));

		assertSameList(Collections.emptyList(), result);

		t$1.completeSuccessfully("foobar");
		assertSameList(Collections.singletonList(6), result);
	}

	@Test
	public void testSuccessfulTaskMap() {
		List<String> result = new ArrayList<>();
		Task<String> t$1 = SuccessfulTask.instance.map($ -> "hello");
		t$1.forEach((s,e) -> {
			assertNull(e);
			result.add(s);
		});
		assertSameList(Collections.singletonList("hello"), result);
	}

	@Test
	public void testFailureTaskMap() {
		List<Exception> result = new ArrayList<>();
		Task<String> t$1 = FailedTask.instance.map($ -> "goodbye");
		t$1.forEach((s,e) -> {
			assertNull(s);
			result.add(e);
		});
		assertSameList(Collections.singletonList(FailedTask.GENERIC_FAILURE), result);
	}

	@Test
	public void testIncompleteTaskMap() {
		List<Object> result = new ArrayList<>();
		Task<String> t$1 = IncompleteTask.instance.map($ -> "worldly");
		t$1.forEach((o,e) -> {
			result.add(o);
			result.add(e);
		});
		assertSameList(Collections.emptyList(), result);
	}

	@Test
	public void testMappedTaskMap() {
		List<Integer> result = new ArrayList<>();
		MappedTask<Void,String> t$1 = new MappedTask<>(SuccessfulTask.instance, $ -> "hello world");
		Task<Integer> t$2 = t$1.map(String::length);
		t$2.forEach((i,e) -> {
			result.add(i);
		});
		assertSameList(Collections.singletonList(11), result);
	}
}
