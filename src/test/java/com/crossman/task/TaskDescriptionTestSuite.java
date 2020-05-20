package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

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

	@Test
	public void testToTask() {
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
		assertFalse(task.isTaskCompleted());
		assertFalse(task.isTaskSucceeded());

		task.setTaskCompleted(true);
		assertFalse(task.isTaskCompleted());
		assertFalse(task.isTaskSucceeded());

		task.setTaskSucceeded(true);
		assertFalse(task.isTaskCompleted());
		assertFalse(task.isTaskSucceeded());

		task.setTaskCompleted(true, Task.SetterOption.CASCADE);
		assertTrue(task.isTaskCompleted());
		assertFalse(task.isTaskSucceeded());

		task.setTaskSucceeded(true, Task.SetterOption.CASCADE);
		assertTrue(task.isTaskCompleted());
		assertTrue(task.isTaskSucceeded());
	}
}
