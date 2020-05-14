package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A Task has a concept of completing successfully, completing unsuccessfully,
 * and being incomplete. Subtypes expand on these basic concepts.
 */
public interface Task<T> {
	public static final Task<Void> success = SuccessfulTask.instance;
	public static final Task<Void> failure = FailedTask.instance;
	public static final Task<Void> incomplete = IncompleteTask.instance;

	public void forEach(BiConsumer<T,Exception> blk);

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
