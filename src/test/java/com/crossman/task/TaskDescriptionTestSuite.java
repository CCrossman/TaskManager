package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class TaskDescriptionTestSuite {

	@Test
	public void testTaskSerialization() throws IOException, ClassNotFoundException {
		Task task = Task.make(b -> {
			b.withTitle("Morning Routine");
			b.withDescription("Go through your morning routine.");
			b.withChild(Task.incomplete("Wake Up!", "Wake up from your dream."));
			b.withChild(Task.incomplete("Get Up!", "Get out of bed."));
			b.withChild(b2 -> {
				b2.withTitle("Shower!");
				b2.withDescription("Clean up in the shower");
				b2.withChild(Task.incomplete("Get in the shower."));
				b2.withChild(Task.incomplete("Sing it!"));
				b2.withChild(Task.incomplete("Get out of the shower."));
			});
			b.withChild(b2 -> {
				b2.withDescription("Make some coffee.");
				b2.withChild(Task.incomplete("Pour some coffee grounds."));
				b2.withChild(Task.incomplete("Pour some water."));
				b2.withChild(Task.incomplete("Brew the coffee."));
				b2.withChild(Task.incomplete("Drink the coffee."));
			});
		});

		assertNotNull(task);

		File file = File.createTempFile("task", "" + System.currentTimeMillis());
		try {
			// serialize to file
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
				oos.writeObject(task);
			}
			// deserialize from file
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				Task td = (Task) ois.readObject();
				assertEquals(task,td);
			}
		} finally {
			file.deleteOnExit();
		}
	}

//	@Test
//	public void testToTask() {
//		TaskBuilder taskBuilder = new TaskBuilder()
//			.setTitle("Morning Routine")
//			.setDescription("Go through your morning routine.")
//			.addTask("Wake Up!", "Wake up from your dream.")
//			.addTask("Get Up!", "Get out of bed.")
//			.addTask("Shower!", "Clean up in the shower", tb -> {
//				tb.addTask("Get in the shower.");
//				tb.addTask("Sing 'Don't Stop Believing'");
//				tb.addTask("Get out of the shower.");
//			})
//			.addTask("Coffee Time", "Brew up some coffee.", tb -> {
//				tb.addTask("Pour some coffee grounds.");
//				tb.addTask("Pour some water.");
//				tb.addTask("Brew the coffee.");
//				tb.addTask("Drink the coffee.");
//			});
//
//		TaskDescription taskDescription = taskBuilder.build();
//
//		TaskInstance task = taskDescription.toTask();
//		assertNotNull(task);
//		assertFalse(task.isCompleted());
//		task.setCompleted(true);
//		assertTrue(task.isCompleted());
//		assertFalse(task.isTreeCompleted());
//		task.setDescendantsCompleted(true);
//		assertTrue(task.isCompleted());
//	}
}
