package com.crossman.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class RequiresAnySuccesses implements Task {
	private final Collection<Task> tasks;

	public RequiresAnySuccesses(Collection<Task> tasks) {
		this.tasks = new ArrayList<>(tasks);
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
		boolean sum = false;
		for (Task task : tasks) {
			if (task.isNotCompleted()) {
				return Optional.empty();
			}
			Optional<Boolean> o = task.isSuccess();
			if (o.isEmpty()) {
				return Optional.empty();
			}
			sum = sum || o.get();
		}
		return Optional.of(sum);
	}

	public static RequiresAnySuccesses of(Task... tasks) {
		return new RequiresAnySuccesses(Arrays.asList(tasks));
	}
}
