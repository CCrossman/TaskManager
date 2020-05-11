package com.crossman.task;

import java.util.Objects;
import java.util.Optional;

public final class BasicTask implements Task {
	private boolean completed;
	private Boolean succeeded;

	public BasicTask() {
		completed = false;
		succeeded = null;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.ofNullable(succeeded);
	}

	public void completeSuccessfully() {
		completed = true;
		succeeded = true;
	}

	public void completeUnsuccessfully() {
		completed = true;
		succeeded = false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BasicTask basicTask = (BasicTask) o;
		return isCompleted() == basicTask.isCompleted() &&
				Objects.equals(succeeded, basicTask.succeeded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(isCompleted(), succeeded);
	}

	@Override
	public String toString() {
		return "BasicTask{" +
				"completed=" + completed +
				", succeeded=" + succeeded +
				'}';
	}
}
