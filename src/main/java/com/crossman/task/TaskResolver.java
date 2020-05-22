package com.crossman.task;

public interface TaskResolver {
	public boolean resolveSucceeded(Task.Node node);
	public boolean resolveCompleted(Task.Node node);
}
