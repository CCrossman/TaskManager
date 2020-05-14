package com.crossman.task;

import java.util.*;
import java.util.function.BiConsumer;

public final class BasicTask<T> implements Task<T> {
	private final Deque<BiConsumer<T,Exception>> pendingConsumers = new ArrayDeque<>();

	private T value;
	private Exception exception;

	public BasicTask() {
		value = null;
		exception = null;
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
	public Optional<Boolean> isSuccess() {
		if (value == null && exception == null) {
			return Optional.empty();
		}
		return Optional.of(exception == null);
	}

	public void completeSuccessfully(T value) {
		if (this.value == null && exception == null) {
			this.value = value;

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}

	public void completeUnsuccessfully(Exception exception) {
		if (value == null && this.exception == null) {
			this.exception = exception;

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}
}
