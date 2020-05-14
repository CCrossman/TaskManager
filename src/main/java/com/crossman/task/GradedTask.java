package com.crossman.task;

import com.crossman.util.ExceptionList;
import com.crossman.util.Grade;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A GradedTask completes when all its child tasks complete. The resulting
 * success or failure of the task depends on the "grader" used in combination
 * with the results of the child tasks.
 *
 * @param <T>
 */
public final class GradedTask<T> implements Task<Collection<T>> {
	private final Deque<BiConsumer<Collection<T>,Exception>> pendingConsumers = new ArrayDeque<>();

	private final Collection<Task<T>> tasks;
	private final Function<Grade,Optional<Boolean>> grader;

	private boolean completed = false;

	private transient Collection<T> successes;
	private transient ExceptionList failures;

	public GradedTask(Collection<Task<T>> tasks, Function<Grade, Optional<Boolean>> grader) {
		this.tasks = new ArrayList<>(tasks);
		this.grader = grader;

		if (tasks.isEmpty()) {
			completed = true;
			successes = Collections.emptyList();
			failures  = null;
		} else {
			AtomicInteger completedTaskCounter = new AtomicInteger(0);
			List<T> _successes = new CopyOnWriteArrayList<>();
			List<Exception> _failures = new CopyOnWriteArrayList<>();

			for (Task<T> task : tasks) {
				task.forEach((t, e) -> {
					int i = completedTaskCounter.incrementAndGet();

					if (e != null) {
						_failures.add(e);
					} else {
						_successes.add(t);
					}

					if (i == tasks.size()) {
						completed = true;

						if (!_failures.isEmpty()) {
							successes = null;
							failures = new ExceptionList(_failures);
						} else {
							successes = _successes;
							failures = null;
						}

						while (!pendingConsumers.isEmpty()) {
							pendingConsumers.poll().accept(successes, failures);
						}
					}
				});
			}
		}
	}

	@Override
	public void forEach(BiConsumer<Collection<T>, Exception> blk) {
		if (isCompleted()) {
			blk.accept(successes, failures);
		} else {
			pendingConsumers.add(blk);
		}
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		int numSuccesses = 0;
		int numFailures = 0;
		int numIncomplete = 0;

		for (Task<? extends T> task : tasks) {
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

	public static <T,E extends Exception> GradedTask<T> of(Function<Grade,Optional<Boolean>> grader, Task<T>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks),grader);
	}

	public static <T,E extends Exception> GradedTask<T> requiresAllSuccesses(Task<T>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() == grade.getNumResults());
		});
	}

	public static <T,E extends Exception> GradedTask<T> requiresAnySuccesses(Task<T>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() > 0);
		});
	}

	public static <T,E extends Exception> GradedTask<T> requiresSomeSuccesses(int howMany, Task<T>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() >= howMany);
		});
	}
}
