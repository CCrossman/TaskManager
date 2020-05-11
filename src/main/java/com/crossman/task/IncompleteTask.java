package com.crossman.task;

import java.util.Optional;

public enum IncompleteTask implements Task {
	instance;

	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.empty();
	}
}
