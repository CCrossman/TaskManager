package com.crossman.task;

import com.crossman.util.Grade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * A GradedTask aggregates the success, failure, and completeness
 * of sub-tasks before feeding them to a function that produces
 * the final result of this task.
 *
 * A GradedTask is immutable if its sub-tasks are immutable.
 */
public final class GradedTask implements Task {
	private final Iterable<Task> tasks;
	private final Function<Grade,Optional<Boolean>> grader;

	public GradedTask(Collection<Task> tasks, Function<Grade, Optional<Boolean>> grader) {
		this.tasks = new ArrayList<>(tasks);
		this.grader = grader;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		int numSuccesses = 0;
		int numFailures = 0;
		int numIncomplete = 0;

		for (Task task : tasks) {
			Optional<Boolean> o = task.isSuccess();
			if (o.isPresent()) {
				if (o.get()) {
					numSuccesses++;
				} else {
					numFailures++;
				}
			} else {
				numIncomplete++;
			}
		}

		return grader.apply(new Grade(numSuccesses,numFailures,numIncomplete));
	}

	public static GradedTask of(Function<Grade,Optional<Boolean>> grader, Task... tasks) {
		return new GradedTask(Arrays.asList(tasks),grader);
	}

	public static GradedTask requiresAllSuccesses(Task... tasks) {
		return new GradedTask(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() == grade.getNumResults());
		});
	}

	public static GradedTask requiresAnySuccesses(Task... tasks) {
		return new GradedTask(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() > 0);
		});
	}

	public static GradedTask requiresSomeSuccesses(int howMany, Task... tasks) {
		return new GradedTask(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() >= howMany);
		});
	}
}
