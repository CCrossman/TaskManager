package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A Task has a concept of completing successfully, completing unsuccessfully,
 * and being incomplete. Subtypes expand on these basic concepts.
 */
public interface Task<T> {
	public static final Task<Void> success = SuccessfulTask.instance;
	public static final Task<Void> failure = FailedTask.instance;
	public static final Task<Void> incomplete = IncompleteTask.instance;

	public default <U> Task<U> flatMap(Function<T,Task<U>> fn) {
		BasicTask<U> task = BasicTask.incomplete();
		forEach((t,e1) -> {
			if (e1 != null) {
				task.completeUnsuccessfully(e1);
			} else {
				fn.apply(t).forEach((u,e2) -> {
					if (e2 != null) {
						task.completeUnsuccessfully(e2);
					} else {
						task.completeSuccessfully(u);
					}
				});
			}
		});
		return task;
	}

	public void forEach(BiConsumer<T,Exception> blk);

	public default boolean isCompleted() {
		return isSuccess().isPresent();
	}

	public default boolean isNotCompleted() {
		return !isCompleted();
	}

	public default <U> Task<U> map(Function<T,U> fn) {
		return new MappedTask<>(this,fn);
	}

	public Optional<Boolean> isSuccess();

	public default Optional<Boolean> isFailure() {
		return isSuccess().map(b -> !b);
	}

	public static <T> Task<T> constant(T t) {
		return success.map($ -> t);
	}

	public static <T> Task<T> join(Task<Task<T>> task) {
		return task.flatMap(Function.identity());
	}
}
