package com.crossman.task;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.crossman.util.Preconditions.checkNotNull;

public final class Task implements Serializable {
	private final Task.Node root;

	public Task(Node root) {
		this.root = checkNotNull(root);
	}

	public void eachChild(Consumer<Task> blk) {
		for (Node child : root.getChildren()) {
			blk.accept(new Task(child));
		}
	}

	public void eachPreOrder(Consumer<Task> blk) {
		eachPreOrder(root,blk);
	}

	private void eachPreOrder(Node node, Consumer<Task> blk) {
		if (node != null) {
			blk.accept(new Task(node));
			node.getChildren().forEach(n -> eachPreOrder(n, blk));
		}
	}

	public void eachPostOrder(Consumer<Task> blk) {
		eachPostOrder(root,blk);
	}

	private void eachPostOrder(Node node, Consumer<Task> blk) {
		if (node != null) {
			node.getChildren().forEach(n -> eachPostOrder(n, blk));
			blk.accept(new Task(node));
		}
	}

	public Optional<Task> findChild(Predicate<? super Task> predicate) {
		Optional<Node> o = root.getChildren().stream().filter(n -> predicate.test(new Task(n))).findFirst();
		return o.map(Task::new);
	}

	public Optional<Task> findDescendant(Predicate<? super Task> predicate) {
		return foldPreOrder(Optional.empty(), (o,t) -> {
			if (o.isPresent()) {
				return o;
			}
			if (predicate.test(t)) {
				return Optional.of(t);
			}
			return Optional.empty();
		});
	}

	public <T> T foldPreOrder(T initial, BiFunction<T,Task,T> fn) {
		return root.foldSelfAndDescendants(initial,(t,n) -> fn.apply(t,new Task(n)));
	}

	public <T> T foldPostOrder(T initial, BiFunction<T,Task,T> fn) {
		return root.foldDescendantsAndSelf(initial,(t,n) -> fn.apply(t,new Task(n)));
	}

	public Task getChild(int i) {
		Node n = root.getChildren().get(i);
		return new Task(n);
	}

	public Node getRoot() {
		return root;
	}

	public boolean isTaskCompleted() {
		return root.getResolver().resolveCompleted(root);
	}

	public void setTaskCompleted(boolean completed, Task.SetterOption... setterOptions) {
		Set<Task.SetterOption> options = new HashSet<>(Arrays.asList(setterOptions));
		if (options.contains(SetterOption.CASCADE)) {
			eachPostOrder(n -> n.setTaskCompleted(completed));
		} else {
			root.setCompleted(completed);
		}
	}

	public boolean isTaskSucceeded() {
		return root.getResolver().resolveSucceeded(root);
	}

	public void setTaskSucceeded(boolean succeeded, Task.SetterOption... setterOptions) {
		Set<Task.SetterOption> options = new HashSet<>(Arrays.asList(setterOptions));
		if (options.contains(SetterOption.CASCADE)) {
			eachPostOrder(n -> n.setTaskSucceeded(succeeded));
		} else {
			root.setSucceeded(succeeded);
		}
	}

	public Optional<String> getTitle() {
		return root.getTitle();
	}

	public String getDescription() {
		return root.getDescription();
	}

	public Serializable getProperty(String key) {
		return root.getProperty(key);
	}

	public Serializable setProperty(String key, Serializable value) {
		return root.setProperty(key, value);
	}

	public Map<String, Serializable> getProperties() {
		return root.getProperties();
	}

	public List<Task> getChildren() {
		return root.getChildren().stream().map(Task::new).collect(Collectors.toList());
	}

	public Exception getException() {
		return root.getException();
	}

	public void setException(Exception exception) {
		root.setException(exception);
	}

	public boolean isCompleted() {
		return root.isCompleted();
	}

	public boolean isSucceeded() {
		return root.isSucceeded();
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

	public static enum SetterOption {
		CASCADE
	}

	public static final class Node implements Serializable {
		private final String title;
		private final String description;
		private final Map<String,Serializable> properties;
		private final List<Node> children;
		private final TaskResolver resolver;

		private boolean completed;
		private boolean succeeded;
		private Exception exception;

		public Node(String title, String description, Map<String, Serializable> properties, List<Node> children, TaskResolver resolver, boolean completed, boolean succeeded) {
			this.title = title;
			this.description = checkNotNull(description);
			this.properties = new HashMap<>(properties);
			this.children = new ArrayList<>(children);
			this.resolver = resolver;
			this.completed = completed;
			this.succeeded = succeeded;
		}

		public Optional<String> getTitle() {
			return Optional.ofNullable(title);
		}

		public String getDescription() {
			return description;
		}

		public Serializable getProperty(String key) {
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

		public TaskResolver getResolver() {
			return resolver;
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

		private <T> T foldDescendantsAndSelf(T initial, BiFunction<T,Node,T> fn) {
			T sum = initial;
			for (Node child : children) {
				sum = child.foldDescendantsAndSelf(sum,fn);
			}
			return fn.apply(sum,this);
		}

		private <T> T foldSelfAndDescendants(T initial, BiFunction<T,Node,T> fn) {
			T sum = fn.apply(initial,this);
			for (Node child : children) {
				sum = child.foldSelfAndDescendants(sum,fn);
			}
			return sum;
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
					getResolver().equals(node.getResolver()) &&
					Objects.equals(getException(), node.getException());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getTitle(), getDescription(), getProperties(), getChildren(), getResolver(), isCompleted(), isSucceeded(), getException());
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
			private TaskResolver resolver;

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

			public Builder withResolver(TaskResolver resolver) {
				this.resolver = resolver;
				return this;
			}

			public Node build() {
				if (resolver == null) {
					resolver = DefaultTaskResolver.instance;
				}
				final Node node = new Node(title, description, properties, children.stream().map(Supplier::get).collect(Collectors.toList()), resolver, completed, succeeded);
				node.setException(exception);
				return node;
			}
		}
	}
}
