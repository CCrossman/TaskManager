package com.crossman.task;

import java.util.Optional;

/**
 * A Task has a concept of completing successfully, completing unsuccessfully,
 * and being incomplete. Subtypes expand on these basic concepts.
 */
public interface Task {
	public static final Task success = SuccessfulTask.instance;
	public static final Task failure = FailedTask.instance;
	public static final Task incomplete = IncompleteTask.instance;

	public default boolean isCompleted() {
		return isSuccess().isPresent();
	}

	public default boolean isNotCompleted() {
		return !isCompleted();
	}

	public Optional<Boolean> isSuccess();

	public default Optional<Boolean> isFailure() {
		return isSuccess().map(b -> !b);
	}
}
