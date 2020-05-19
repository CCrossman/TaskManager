package com.crossman.task;

import java.util.*;

import static com.crossman.util.Preconditions.checkNotNull;

public final class TaskInstance {
	private final String description;
	private final Collection<TaskInstance> subTasks;
	private final Map<String,Object> properties;

	private TaskInstance parent;
	private boolean completed;
	private boolean success;

	public TaskInstance(String description, Collection<TaskInstance> subTasks, Map<String,Object> properties, boolean completed, boolean success) {
		this.description = checkNotNull(description);
		this.subTasks = new ArrayList<>(checkNotNull(subTasks));
		this.properties = new HashMap<>(properties);
		this.completed = completed;
		this.success = success;

		subTasks.forEach(ti -> ti.setParent(this));
	}

	public TaskInstance getParent() {
		return parent;
	}

	private void setParent(TaskInstance ti) {
		this.parent = ti;
	}

	public String getDescription() {
		return description;
	}

	public Collection<TaskInstance> getSubTasks() {
		return subTasks;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object getAncestralProperty(String key) {
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		if (parent != null) {
			return parent.getProperty(key);
		}
		return null;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public Object setProperty(String key, Object value) {
		return properties.put(key,value);
	}

	public Object removeProperty(String key) {
		return properties.remove(key);
	}

	public boolean isTreeCompleted() {
		return isCompleted() && subTasks.stream().allMatch(ti -> ti.isTreeCompleted());
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setDescendantsCompleted(boolean completed) {
		subTasks.forEach(ti -> ti.setDescendantsCompleted(completed));
		setCompleted(completed);
	}

	public boolean isTreeSuccess() {
		return isSuccess() && subTasks.stream().allMatch(ti -> ti.isTreeSuccess());
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setDescendantsSuccess(boolean success) {
		subTasks.forEach(ti -> ti.setDescendantsSuccess(success));
		setSuccess(success);
	}
}
