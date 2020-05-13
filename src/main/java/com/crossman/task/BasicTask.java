package com.crossman.task;

import java.util.Objects;
import java.util.Optional;

public final class BasicTask implements Task {
	private Boolean succeeded;

	public BasicTask() {
		succeeded = null;
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return Optional.ofNullable(succeeded);
	}

	public void completeSuccessfully() {
		succeeded = true;
	}

	public void completeUnsuccessfully() {
		succeeded = false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BasicTask basicTask = (BasicTask) o;
		return Objects.equals(succeeded, basicTask.succeeded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(succeeded);
	}

	@Override
	public String toString() {
		return "BasicTask{" +
				"succeeded=" + succeeded +
				'}';
	}
}
