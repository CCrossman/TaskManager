package com.crossman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ExceptionList extends Exception {
	private final List<Exception> exceptionList;

	public ExceptionList(List<Exception> exceptionList) {
		this.exceptionList = new ArrayList<>(exceptionList);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ExceptionList that = (ExceptionList) o;
		return exceptionList.equals(that.exceptionList);
	}

	public int size() {
		return exceptionList.size();
	}

	public Exception get(int index) {
		return exceptionList.get(index);
	}

	@Override
	public int hashCode() {
		return Objects.hash(exceptionList);
	}

	@Override
	public String toString() {
		return "ExceptionList{" +
				"exceptionList=" + exceptionList +
				'}';
	}
}
