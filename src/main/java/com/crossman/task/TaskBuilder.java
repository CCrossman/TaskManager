package com.crossman.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TaskBuilder {
	private String title;
	private String description;
	private final List<TaskDescription> tasks = new ArrayList<>();

	public TaskBuilder addTask(String description) {
		tasks.add(new SimpleTaskDescription(description));
		return this;
	}

	public TaskBuilder addTask(String title, String description) {
		tasks.add(new TitledTaskDescription(title,description));
		return this;
	}

	public TaskBuilder addTask(String title, String description, Consumer<TaskBuilder> blk) {
		TaskBuilder tb = new TaskBuilder();
		blk.accept(tb);
		tasks.add(new TitledTaskDescription(title,description,tb.tasks));
		return this;
	}

	public String getTitle() {
		return title;
	}

	public TaskBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public TaskBuilder setDescription(String description) {
		this.description = description;
		return this;
	}

	public List<TaskDescription> getTasks() {
		return tasks;
	}

	public TaskDescription build() {
		return new TitledTaskDescription(title, description, tasks);
	}
}
