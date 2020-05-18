package com.crossman.task;

import java.io.Serializable;
import java.util.Objects;

import static com.crossman.util.Preconditions.checkNotNull;

public final class SimpleTaskDescription implements Serializable, TaskDescription {
	private final String description;

	public SimpleTaskDescription(String description) {
		this.description = checkNotNull(description);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleTaskDescription that = (SimpleTaskDescription) o;
		return getDescription().equals(that.getDescription());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDescription());
	}

	@Override
	public String toString() {
		return "SimpleTaskDescription{" +
				"description='" + description + '\'' +
				'}';
	}
}
