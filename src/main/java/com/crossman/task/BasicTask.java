package com.crossman.task;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class BasicTask<T> implements Task<T> {
	private final Deque<BiConsumer<T,Exception>> pendingConsumers = new ArrayDeque<>();

	private boolean completed;
	private T value;
	private Exception exception;

	public BasicTask() {
		completed = false;
		value = null;
		exception = null;
	}

	@Override
	public <U> Task<U> flatMap(Function<T, Task<U>> fn) {
		if (isCompleted()) {
			if (exception != null) {
				return BasicTask.failed(exception);
			}
			return fn.apply(value);
		}
		return BasicTask.incomplete();
	}

	@Override
	public void forEach(BiConsumer<T, Exception> blk) {
		if (isCompleted()) {
			blk.accept(value,exception);
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
		if (isNotCompleted()) {
			return Optional.empty();
		}
		return Optional.of(exception == null);
	}

	public void completeSuccessfully(T value) {
		if (isNotCompleted()) {
			this.completed = true;
			this.value = value;

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}

	public void completeUnsuccessfully(Exception exception) {
		if (isNotCompleted()) {
			this.completed = true;
			this.exception = exception;

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}

	public static <T> BasicTask<T> succeed(T t) {
		BasicTask<T> task = new BasicTask<>();
		task.completeSuccessfully(t);
		return task;
	}

	public static <T> BasicTask<T> failed(Exception e) {
		BasicTask<T> task = new BasicTask<>();
		task.completeUnsuccessfully(e);
		return task;
	}

	public static <T> BasicTask<T> incomplete() {
		return new BasicTask<>();
	}
}
