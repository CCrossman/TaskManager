package com.crossman.task;

import com.crossman.util.ExceptionList;
import com.crossman.util.Grade;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class GradedTask<T,E extends Exception> implements Task<Collection<T>, ExceptionList> {
	private final Collection<Listener<Collection<T>,ExceptionList>> listeners = new ArrayList<>();
	private final Deque<BiConsumer<Collection<T>,ExceptionList>> pendingConsumers = new ArrayDeque<>();

	private final Collection<Task<T,E>> tasks;
	private final Function<Grade,Optional<Boolean>> grader;

	private boolean completed = false;

	private transient Collection<T> successes;
	private transient ExceptionList failures;

	public GradedTask(Collection<Task<T,E>> tasks, Function<Grade, Optional<Boolean>> grader) {
		this.tasks = new ArrayList<>(tasks);
		this.grader = grader;

		Semaphore semaphore = new Semaphore(tasks.size());
		Collection<T> _successes = new ConcurrentLinkedQueue<>();
		Collection<E> _failures = new ConcurrentLinkedQueue<>();

		for (Task<T, E> task : tasks) {
			semaphore.acquireUninterruptibly();

			Listener<T, E> lstr = new Listener<>() {
				@Override
				public void onComplete(T value, E error) {
					semaphore.release();

					if (value != null) {
						_successes.add(value);
					}
					if (error != null) {
						_failures.add(error);
					}

					if (semaphore.availablePermits() == tasks.size()) {
						completed = true;
						successes = _successes;
						failures = new ExceptionList(new ArrayList<>(_failures));

						for (Listener<Collection<T>,ExceptionList> listener : listeners) {
							listener.onComplete(successes,failures);
						}

						while (!pendingConsumers.isEmpty()) {
							pendingConsumers.poll().accept(successes,failures);
						}
					}
				}
			};
			task.addListener(lstr);
		}
	}

	@Override
	public void addListener(Listener<Collection<T>, ExceptionList> listener) {
		listeners.add(listener);
	}

	@Override
	public void forEach(BiConsumer<Collection<T>, ExceptionList> blk) {
		if (isCompleted()) {
			blk.accept(successes,failures);
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

		for (Task<? extends T,? extends E> task : tasks) {
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

	public static <T,E extends Exception> GradedTask<T,E> of(Function<Grade,Optional<Boolean>> grader, Task<T,E>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks),grader);
	}

	public static <T,E extends Exception> GradedTask<T,E> requiresAllSuccesses(Task<T,E>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() == grade.getNumResults());
		});
	}

	public static <T,E extends Exception> GradedTask<T,E> requiresAnySuccesses(Task<T,E>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() > 0);
		});
	}

	public static <T,E extends Exception> GradedTask<T,E> requiresSomeSuccesses(int howMany, Task<T,E>... tasks) {
		return new GradedTask<>(Arrays.asList(tasks), grade -> {
			if (grade.getNumIncomplete() > 0) {
				return Optional.empty();
			}
			return Optional.of(grade.getNumSuccesses() >= howMany);
		});
	}
}
