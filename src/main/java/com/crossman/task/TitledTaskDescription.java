package com.crossman.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.crossman.util.Preconditions.checkNotNull;

public final class TitledTaskDescription implements Serializable, TaskDescription {
	private final String title;
	private final String description;
	private final List<TaskDescription> subTasks;

	public TitledTaskDescription(String title, String description) {
		this(title, description, Collections.emptyList());
	}

	public TitledTaskDescription(String title, String description, List<TaskDescription> subTasks) {
		this.title = checkNotNull(title);
		this.description = checkNotNull(description);
		this.subTasks = new ArrayList<>(subTasks);
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public TaskInstance toTask() {
		return new TaskInstance(description, subTasks.stream().map(td -> td.toTask()).collect(Collectors.toList()), false, true);
	}

	public List<TaskDescription> getSubTasks() {
		return subTasks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TitledTaskDescription that = (TitledTaskDescription) o;
		return getTitle().equals(that.getTitle()) &&
				getDescription().equals(that.getDescription()) &&
				getSubTasks().equals(that.getSubTasks());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTitle(), getDescription(), getSubTasks());
	}

	@Override
	public String toString() {
		return "TitledTaskDescription{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", subTasks=" + subTasks +
				'}';
	}
}
