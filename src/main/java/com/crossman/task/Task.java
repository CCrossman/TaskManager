package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A Task has a concept of completing successfully, completing unsuccessfully,
 * and being incomplete. Subtypes expand on these basic concepts.
 */
public interface Task<T,E extends Exception> {
	public static final Task<Void,Exception> success = SuccessfulTask.instance;
	public static final Task<Void,Exception> failure = FailedTask.instance;
	public static final Task<Void,Exception> incomplete = IncompleteTask.instance;

	public void addListener(Task.Listener<T,E> listener);

	public void forEach(BiConsumer<T,E> blk);

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

	public static interface Listener<T,E extends Exception> {
		public void onComplete(T value, E error);
	}
}
