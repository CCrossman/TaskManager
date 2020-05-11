package com.crossman.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class RequiresSomeSuccesses implements Task {
	private final int minimumSuccesses;
	private final Collection<Task> tasks;

	public RequiresSomeSuccesses(int minimumSuccesses, Collection<Task> tasks) {
		this.minimumSuccesses = minimumSuccesses;
		this.tasks = tasks;
	}

	@Override
	public boolean isCompleted() {
		return !tasks.isEmpty() && tasks.stream().allMatch(Task::isCompleted);
	}

	@Override
	public Optional<Boolean> isSuccess() {
		if (tasks.isEmpty()) {
			return Optional.empty();
		}
		int numSuccesses = 0;
		for (Task task : tasks) {
			if (task.isNotCompleted()) {
				return Optional.empty();
			}
			Optional<Boolean> o = task.isSuccess();
			if (o.isEmpty()) {
				return Optional.empty();
			}
			if (o.get()) {
				numSuccesses++;
			}
		}
		return Optional.of(numSuccesses >= minimumSuccesses);
	}

	public static RequiresSomeSuccesses of(int minimumSuccesses, Task... tasks) {
		return new RequiresSomeSuccesses(minimumSuccesses, Arrays.asList(tasks));
	}
}
