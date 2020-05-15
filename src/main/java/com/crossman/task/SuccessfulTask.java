package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum SuccessfulTask implements Task<Void> {
	instance;

	@Override
	public <U> Task<U> flatMap(Function<Void, Task<U>> fn) {
		return fn.apply(null);
	}

	@Override
	public void forEach(BiConsumer<Void, Exception> blk) {
		blk.accept(null,null);
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.of(true);
	}
}
