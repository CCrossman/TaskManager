package com.crossman.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.crossman.util.Preconditions.checkNotNull;

public final class Task implements Serializable {
	private final String value;
	private final List<Task> children;

	public Task(String value) {
		this(value, Collections.emptyList());
	}

	public Task(String value, List<Task> children) {
		this.value = checkNotNull(value);
		this.children = new ArrayList<>(children);
	}

	public <T> T fold(T initial, BiFunction<T,String,T> folder) {
		return fold(initial, Order.PRE_ORDER, folder);
	}

	public <T> T fold(T initial, Order order, BiFunction<T,String,T> folder) {
		final AtomicReference<T> folded = new AtomicReference<>(initial);
		forEach(order,s -> {
			folded.getAndUpdate(t -> folder.apply(t,s));
		});
		return folded.get();
	}

	public void forEach(Consumer<String> blk) {
		forEach(Order.PRE_ORDER, blk);
	}

	public void forEach(Task.Order order, Consumer<String> blk) {
		switch (order) {
			case POST_ORDER -> forEachPost(blk);
			case PRE_ORDER -> forEachPre(blk);
		}
	}

	private void forEachPost(Consumer<String> blk) {
		children.forEach(t -> t.forEachPost(blk));
		blk.accept(value);
	}

	private void forEachPre(Consumer<String> blk) {
		blk.accept(value);
		children.forEach(t -> t.forEachPre(blk));
	}

	public String getValue() {
		return value;
	}

	public List<Task> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return getValue().equals(task.getValue()) &&
				getChildren().equals(task.getChildren());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getValue(), getChildren());
	}

	public static enum Order {
		POST_ORDER, PRE_ORDER
	}
}
