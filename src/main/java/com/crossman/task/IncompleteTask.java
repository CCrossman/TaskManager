package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;

public enum IncompleteTask implements Task<Void,Exception> {
	instance;

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
