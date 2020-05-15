package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum IncompleteTask implements Task<Void> {
	instance;

	@Override
	public <U> Task<U> flatMap(Function<Void, Task<U>> fn) {
		return BasicTask.incomplete();
	}

	@Override
	public void forEach(BiConsumer<Void, Exception> blk) {
		// never completes
	}

	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.empty();
	}
}
