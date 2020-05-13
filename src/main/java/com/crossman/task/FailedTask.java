package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;

public enum FailedTask implements Task<Void,Exception> {
	instance;

	@Override
	public void forEach(BiConsumer<Void, Exception> blk) {
		blk.accept(null, new Exception());
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
