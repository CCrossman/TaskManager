package com.crossman.task;

import com.crossman.util.ExceptionList;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

	public default <U extends T> Task<T> recover(Function<Exception,U> fn) {
		return recoverWith(e -> BasicTask.succeed(fn.apply(e)));
	}

	public default <U extends T> Task<T> recoverWith(Function<Exception,Task<U>> fn) {
		BasicTask<T> task = BasicTask.incomplete();
		forEach((t,e1) -> {
			if (e1 != null) {
				try {
					Task<U> uTask = fn.apply(e1);
					uTask.forEach((u,e2) -> {
						if (e2 != null) {
							task.completeUnsuccessfully(e2);
						} else {
							task.completeSuccessfully(u);
						}
					});
				} catch (Exception e3) {
					task.completeUnsuccessfully(e3);
				}
			} else {
				task.completeSuccessfully(t);
			}
		});
		return task;
	}

	public static <T> Task<T> constant(T t) {
		return success.map($ -> t);
	}

	public static <T> Task<T> join(Task<Task<T>> task) {
		return task.flatMap(Function.identity());
	}

	public static <T> Task<List<T>> sequence(List<Task<T>> tasks) {
		final int len = tasks.size();
		final Map<Integer,T> results = new HashMap<>();
		final List<Exception> errors = new ArrayList<>();
		final AtomicInteger completionCounter = new AtomicInteger(0);
		final BasicTask<List<T>> ret = BasicTask.incomplete();

		for (int i = 0; i < len; ++i) {
			final int j = i;

			tasks.get(i).forEach((t, e) -> {
				if (e != null) {
					errors.add(e);
				} else {
					results.put(j,t);
				}

				if (completionCounter.incrementAndGet() == len) {
					if (!errors.isEmpty()) {
						ret.completeUnsuccessfully(new ExceptionList(errors));
					} else {
						final List<T> _results = new ArrayList<>();
						for (int k = 0; k < len; ++k) {
							_results.add(results.get(k));
						}
						ret.completeSuccessfully(_results);
					}
				}
			});
		}

		return ret;
	}
}
