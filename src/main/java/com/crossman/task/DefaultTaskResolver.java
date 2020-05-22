package com.crossman.task;

public enum DefaultTaskResolver implements TaskResolver {
	instance;

	@Override
	public boolean resolveSucceeded(Task.Node node) {
		return node.getChildren().stream().map(n -> n.isSucceeded()).reduce(true, (a,b) -> a && b);
	}

	@Override
	public boolean resolveCompleted(Task.Node node) {
		return node.getChildren().stream().map(n -> n.isCompleted()).reduce(true, (a,b) -> a && b);
	}
}
