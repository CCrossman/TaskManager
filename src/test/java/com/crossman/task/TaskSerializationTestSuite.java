package com.crossman.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class TaskSerializationTestSuite {

	@Test
	public void testTaskSerialization() throws IOException, ClassNotFoundException {
		TaskBuilder taskBuilder = new TaskBuilder()
			.setTitle("Morning Routine")
			.setDescription("Go through your morning routine.")
			.addTask("Wake Up!", "Wake up from your dream.")
			.addTask("Get Up!", "Get out of bed.")
			.addTask("Shower!", "Clean up in the shower", tb -> {
				tb.addTask("Get in the shower.");
				tb.addTask("Sing 'Don't Stop Believing'");
				tb.addTask("Get out of the shower.");
			})
			.addTask("Coffee Time", "Brew up some coffee.", tb -> {
				tb.addTask("Pour some coffee grounds.");
				tb.addTask("Pour some water.");
				tb.addTask("Brew the coffee.");
				tb.addTask("Drink the coffee.");
			});

		assertNotNull(taskBuilder);

		TaskDescription taskDescription = taskBuilder.build();
		assertNotNull(taskDescription);

		File file = File.createTempFile("task", "" + System.currentTimeMillis());
		try {
			// serialize to file
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
				oos.writeObject(taskDescription);
			}
			// deserialize from file
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				TaskDescription td = (TaskDescription)ois.readObject();
				assertEquals(taskDescription,td);
			}
		} finally {
			file.deleteOnExit();
		}
	}
}
