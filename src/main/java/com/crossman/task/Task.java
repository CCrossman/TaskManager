package com.crossman.task;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.crossman.util.Preconditions.checkNotNull;

public final class Task implements Serializable {
	private final Task.Node root;

	public Task(Node root) {
		this.root = root;
	}

	public <T> T fold(T initial, BiFunction<T,Node,T> fn) {
		if (root == null) {
			return initial;
		}
		return root.foldDescendantsAndSelf(initial,fn);
	}

	public boolean isTaskCompleted() {
		if (root == null) {
			return false;
		}
		return root.foldDescendantsAndSelf(true, (b,n) -> b && n.isCompleted());
	}

	public boolean isTaskSucceeded() {
		if (root == null) {
			return false;
		}
		return root.foldDescendantsAndSelf(true, (b,n) -> b && n.isSucceeded());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return Objects.equals(root, task.root);
	}

	@Override
	public int hashCode() {
		return Objects.hash(root);
	}

	public static Task incomplete(String description) {
		return make(b -> {
			b.withDescription(description);
		});
	}

	public static Task incomplete(String title, String description) {
		return make(b -> {
			b.withTitle(title);
			b.withDescription(description);
		});
	}

	public static Task succeeded(String title, String description) {
		return make(b -> {
			b.withTitle(title);
			b.withDescription(description);
			b.withCompleted(true);
			b.withSucceeded(true);
		});
	}

	public static Task failed(String title, String description) {
		return make(b -> {
			b.withTitle(title);
			b.withDescription(description);
			b.withCompleted(true);
			b.withSucceeded(false);
		});
	}

	public static Task failed(String title, String description, Exception exception) {
		return make(b -> {
			b.withTitle(title);
			b.withDescription(description);
			b.withCompleted(true);
			b.withSucceeded(false);
			b.withException(exception);
		});
	}

	public static Task make(Consumer<Node.Builder> blk) {
		Node.Builder b = new Node.Builder();
		blk.accept(b);
		return new Task(b.build());
	}

	public static final class Node implements Serializable {
		private final String title;
		private final String description;
		private final Map<String,Serializable> properties;
		private final List<Node> children;

		private boolean completed;
		private boolean succeeded;
		private Exception exception;

		public Node(String description) {
			this(null,description,Collections.emptyMap(),Collections.emptyList(),false,true);
		}

		public Node(String title, String description) {
			this(title,description,Collections.emptyMap(),Collections.emptyList(),false,true);
		}

		public Node(String title, String description, Map<String, Serializable> properties, List<Node> children, boolean completed, boolean succeeded) {
			this.title = title;
			this.description = checkNotNull(description);
			this.properties = new HashMap<>(properties);
			this.children = new ArrayList<>(children);
			this.completed = completed;
			this.succeeded = succeeded;
		}

		public Optional<String> getTitle() {
			return Optional.ofNullable(title);
		}

		public String getDescription() {
			return description;
		}

		public Object getProperty(String key) {
			return properties.get(key);
		}

		private Serializable setProperty(String key, Serializable value) {
			return properties.put(key,value);
		}

		public Map<String, Serializable> getProperties() {
			return Collections.unmodifiableMap(properties);
		}

		public List<Node> getChildren() {
			return Collections.unmodifiableList(children);
		}

		public Exception getException() {
			return exception;
		}

		private void setException(Exception exception) {
			this.exception = exception;
		}

		public boolean isCompleted() {
			return completed;
		}

		private void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public boolean isSucceeded() {
			return succeeded && (exception == null);
		}

		private void setSucceeded(boolean succeeded) {
			this.succeeded = succeeded;
		}

		public <T> T foldDescendantsAndSelf(T initial, BiFunction<T,Node,T> fn) {
			T sum = initial;
			for (Node child : children) {
				sum = child.foldDescendantsAndSelf(sum,fn);
			}
			return fn.apply(sum,this);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Node node = (Node) o;
			return isCompleted() == node.isCompleted() &&
					isSucceeded() == node.isSucceeded() &&
					Objects.equals(getTitle(), node.getTitle()) &&
					getDescription().equals(node.getDescription()) &&
					getProperties().equals(node.getProperties()) &&
					getChildren().equals(node.getChildren()) &&
					Objects.equals(getException(), node.getException());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getTitle(), getDescription(), getProperties(), getChildren(), isCompleted(), isSucceeded(), getException());
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private final List<Supplier<Node>> children = new ArrayList<>();
			private final Map<String,Serializable> properties = new HashMap<>();

			private String title;
			private String description;
			private boolean completed;
			private boolean succeeded;
			private Exception exception;

			public Builder() {}

			public Builder withTitle(String title) {
				this.title = title;
				return this;
			}

			public Builder withDescription(String description) {
				this.description = description;
				return this;
			}

			public Builder withCompleted(boolean completed) {
				this.completed = completed;
				return this;
			}

			public Builder withSucceeded(boolean succeeded) {
				this.succeeded = succeeded;
				return this;
			}

			public Builder withException(Exception exception) {
				this.exception = exception;
				return this;
			}

			public Builder withChild(Node node) {
				this.children.add(() -> node);
				return this;
			}

			public Builder withChild(Task tree) {
				return withChild(tree.root);
			}

			public Builder withChild(Consumer<Builder> blk) {
				Builder b = new Builder();
				blk.accept(b);
				this.children.add(b::build);
				return this;
			}

			public Builder withProperty(String key, Serializable value) {
				this.properties.put(key,value);
				return this;
			}

			public Node build() {
				final Node node = new Node(title, description, properties, children.stream().map(Supplier::get).collect(Collectors.toList()), completed, succeeded);
				node.setException(exception);
				return node;
			}
		}
	}
}
