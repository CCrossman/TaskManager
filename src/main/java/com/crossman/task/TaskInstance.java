package com.crossman.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static com.crossman.util.Preconditions.checkNotNull;

public final class TaskInstance {
	private final String description;
	private final Collection<TaskInstance> subTasks;
	private boolean completed;
	private boolean success;

	public TaskInstance(String description, Collection<TaskInstance> subTasks, boolean completed, boolean success) {
		this.description = checkNotNull(description);
		this.subTasks = new ArrayList<>(checkNotNull(subTasks));
		this.completed = completed;
		this.success = success;
	}

	public String getDescription() {
		return description;
	}

	public Collection<TaskInstance> getSubTasks() {
		return subTasks;
	}

	public boolean isCompleted() {
		return completed && subTasks.stream().allMatch(ti -> ti.isCompleted());
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setDescendantsCompleted(boolean completed) {
		subTasks.forEach(ti -> ti.setDescendantsCompleted(completed));
		setCompleted(completed);
	}

	public boolean isSuccess() {
		return success && subTasks.stream().allMatch(ti -> ti.isSuccess());
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setDescendantsSuccess(boolean success) {
		subTasks.forEach(ti -> ti.setDescendantsSuccess(success));
		setSuccess(success);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskInstance that = (TaskInstance) o;
		return isCompleted() == that.isCompleted() &&
				isSuccess() == that.isSuccess() &&
				getDescription().equals(that.getDescription()) &&
				getSubTasks().equals(that.getSubTasks());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDescription(), getSubTasks(), isCompleted(), isSuccess());
	}

	@Override
	public String toString() {
		return "TaskInstance{" +
				"description='" + description + '\'' +
				", subTasks=" + subTasks +
				", completed=" + completed +
				", success=" + success +
				'}';
	}
}
