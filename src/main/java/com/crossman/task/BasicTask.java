package com.crossman.task;

import java.util.*;
import java.util.function.BiConsumer;

public final class BasicTask<T,E extends Exception> implements Task<T,E> {
	private final Collection<Listener<T,E>> listeners = new ArrayList<>();
	private final Deque<BiConsumer<T,E>> pendingConsumers = new ArrayDeque<>();

	private T value;
	private E exception;

	public BasicTask() {
		value = null;
		exception = null;
	}

	@Override
	public void addListener(Listener<T, E> listener) {
		listeners.add(listener);
	}

	@Override
	public void forEach(BiConsumer<T, E> blk) {
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

			for (Listener<T,E> listener : listeners) {
				listener.onComplete(value,exception);
			}

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}

	public void completeUnsuccessfully(E exception) {
		if (value == null && this.exception == null) {
			this.exception = exception;

			for (Listener<T,E> listener : listeners) {
				listener.onComplete(value,exception);
			}

			while (!pendingConsumers.isEmpty()) {
				pendingConsumers.poll().accept(value,exception);
			}
		}
	}
}
