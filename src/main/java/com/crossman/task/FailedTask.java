package com.crossman.task;

import java.util.Optional;

public enum FailedTask implements Task {
	instance;

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.of(false);
	}
}
