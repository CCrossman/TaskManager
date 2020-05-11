package com.crossman.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class RequiresAllSuccesses implements Task {
	private final Collection<Task> tasks;

	public RequiresAllSuccesses(Collection<Task> tasks) {
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
		boolean sum = true;
		for (Task task : tasks) {
			if (task.isNotCompleted()) {
				return Optional.empty();
			}
			Optional<Boolean> opt = task.isSuccess();
			if (opt.isEmpty()) {
				return Optional.empty();
			}
			sum = sum && opt.get();
		}
		return Optional.of(sum);
	}

	public static RequiresAllSuccesses of(Task... tasks) {
		return new RequiresAllSuccesses(Arrays.asList(tasks));
	}
}
