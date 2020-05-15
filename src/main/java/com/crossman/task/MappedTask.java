package com.crossman.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class MappedTask<T,U> implements Task<U> {
	private final Task<T> task;
	private final Function<T,U> mapper;

	public MappedTask(Task<T> task, Function<T, U> mapper) {
		this.task = task;
		this.mapper = mapper;
	}

	@Override
	public void forEach(BiConsumer<U, Exception> blk) {
		task.forEach((t,e) -> {
			if (e != null) {
				blk.accept(null,e);
			} else {
				blk.accept(mapper.apply(t),null);
			}
		});
	}

	@Override
	public <V> Task<V> map(Function<U,V> fn) {
		return new MappedTask<>(task,mapper.andThen(fn));
	}

	@Override
	public Optional<Boolean> isSuccess() {
		return task.isSuccess();
	}
}
