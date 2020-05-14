package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;

public enum FailedTask implements Task<Void> {
	instance;

	public static final Exception GENERIC_FAILURE = new Exception("Task completed with failure.");

	@Override
	public void forEach(BiConsumer<Void, Exception> blk) {
		blk.accept(null, GENERIC_FAILURE);
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.of(false);
	}
}
