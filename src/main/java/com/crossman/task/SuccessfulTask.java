package com.crossman.task;

import java.util.Optional;

public enum SuccessfulTask implements Task {
	instance;

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.of(true);
	}
}
