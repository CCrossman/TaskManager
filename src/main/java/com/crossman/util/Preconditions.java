package com.crossman.util;

public final class Preconditions {
	private Preconditions() {}

	public static <T> T checkNotNull(T t) {
		if (t == null) {
			throw new NullPointerException();
		}
		return t;
	}
}
