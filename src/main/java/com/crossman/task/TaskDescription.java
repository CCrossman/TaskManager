package com.crossman.task;

public interface TaskDescription {
	public String getDescription();

	public TaskInstance toTask();
}