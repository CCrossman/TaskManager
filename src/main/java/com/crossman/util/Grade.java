package com.crossman.util;

import java.util.Objects;

public final class Grade {
	private final int numSuccesses;
	private final int numFailures;
	private final int numIncomplete;

	public Grade(int numSuccesses, int numFailures, int numIncomplete) {
		this.numSuccesses = numSuccesses;
		this.numFailures = numFailures;
		this.numIncomplete = numIncomplete;
	}

	public int getNumSuccesses() {
		return numSuccesses;
	}

	public int getNumFailures() {
		return numFailures;
	}

	public int getNumIncomplete() {
		return numIncomplete;
	}

	public int getNumResults() {
		return numSuccesses + numFailures + numIncomplete;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Grade grade = (Grade) o;
		return numSuccesses == grade.numSuccesses &&
				numFailures == grade.numFailures &&
				numIncomplete == grade.numIncomplete;
	}

	@Override
	public int hashCode() {
		return Objects.hash(numSuccesses, numFailures, numIncomplete);
	}

	@Override
	public String toString() {
		return "Grade{" +
				"numSuccesses=" + numSuccesses +
				", numFailures=" + numFailures +
				", numIncomplete=" + numIncomplete +
				'}';
	}
}
