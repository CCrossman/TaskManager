package com.crossman.manager;

import com.crossman.task.TaskProtos;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.crossman.util.Preconditions.checkNotNull;

public final class TaskManagerView {
	private final TreeView<String> tree;
	private final Collection<Listener> listeners = new ArrayList<>();
	private final ToggleGroup toggleGroup = new ToggleGroup();

	private final FileChooser fileLoader = new FileChooser();
	private final FileChooser fileSaver = new FileChooser();

	private final BidiMap<TreeItem<String>,CheckBox> checkboxes = new DualHashBidiMap<>();
	private final BidiMap<TreeItem<String>,RadioButton> radioButtons = new DualHashBidiMap<>();
	private final BidiMap<TreeItem<String>,Hyperlink> deletes = new DualHashBidiMap<>();
	private final Map<TreeItem<String>,Circle> circles = new HashMap<>();
	private final Map<TreeItem<String>, ZonedDateTime> created = new HashMap<>();
	private final Map<TreeItem<String>, ZonedDateTime> completed = new HashMap<>();

	private TreeItem<String> focus;
	private boolean dirty;
	private File lastLoad;

	public TaskManagerView(TreeView<String> treeView) {
		this.tree = treeView;
		this.focus = treeView.getRoot();

		fileLoader.setTitle("Open Task");

		fileSaver.setTitle("Save Task");
		fileSaver.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task Files", "*.tsk"));
	}

	public void initialize() {
		final TreeItem<String> treeItem = createTreeItem(null, "Tasks", ZonedDateTime.now(), null);
		tree.setRoot(treeItem);
		reportTreeChange();
	}

	public boolean addListener(Listener listener) {
		return listeners.add(listener);
	}

	public boolean isDirty() {
		return dirty;
	}

	private TreeItem<String> createTreeItem(TreeItem<String> parent, String label, ZonedDateTime whenCreated, ZonedDateTime whenCompleted) {
		final Tooltip t = new Tooltip(whenCreated.toString());
		final CheckBox checkBox = createCheckBox();
		final RadioButton radioButton = createRadioButton();

		final TreeItem<String> treeItem;
		if (parent == null) {
			treeItem = new TreeItem<>(label, new HBox(radioButton, checkBox));
		} else {
			final Hyperlink hyperlink = createDeleteLink();

			final Circle ageCircle;
			if (whenCompleted != null) {
				ageCircle = new Circle(8, Color.BLACK);
			} else {
				ageCircle = new Circle(8, Color.WHITE);
				fillByAge(ageCircle, whenCreated);
			}
			Tooltip.install(ageCircle, t);

			treeItem = new TreeItem<>(label, new HBox(ageCircle, radioButton, checkBox, hyperlink));

			circles.put(treeItem, ageCircle);
			deletes.put(treeItem, hyperlink);
		}

		checkboxes.put(treeItem, checkBox);
		created.put(treeItem, whenCreated);
		radioButtons.put(treeItem, radioButton);

		if (whenCompleted != null) {
			setChecked(treeItem, true, whenCompleted);
		}

		return treeItem;
	}

	private static void fillByAge(Circle circle, ZonedDateTime whenCreated) {
		final ZonedDateTime now = ZonedDateTime.now();
		final Duration duration = Duration.between(now, whenCreated).abs();
		if (duration.compareTo(Duration.ofHours(6)) < 0) {
			circle.setFill(Color.GREEN);
		} else if (duration.compareTo(Duration.ofDays(1)) < 0) {
			circle.setFill(Color.GREENYELLOW);
		} else if (duration.compareTo(Duration.ofDays(7)) < 0) {
			circle.setFill(Color.YELLOW);
		} else {
			circle.setFill(Color.RED);
		}
	}

	private static ZonedDateTime toZDT(TaskProtos.Timestamp timestamp) {
		if (timestamp == null || timestamp.getSeconds() == 0) {
			return null;
		}
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp.getSeconds()), ZoneId.systemDefault());
	}

	private static TaskProtos.Timestamp fromZDT(ZonedDateTime zdt) {
		if (zdt == null) {
			return null;
		}
		return TaskProtos.Timestamp.newBuilder().setSeconds(zdt.toInstant().getEpochSecond()).build();
	}

	private Hyperlink createDeleteLink() {
		final Hyperlink hyperlink = new Hyperlink("Delete");
		hyperlink.setOnAction(ae -> {
			final TreeItem<String> deletee = deletes.getKey(hyperlink);
			final TreeItem<String> parent = deletee.getParent();
			if (parent != null) {
				parent.getChildren().remove(deletee);
				reportNodeRemoved(deletee,parent);
				reportNodeUpdated(parent);
			}
		});
		hyperlink.setOnKeyPressed(ke -> {
			for (Listener listener : listeners) {
				listener.keyPressed(ke);
			}
		});
		return hyperlink;
	}

	private RadioButton createRadioButton() {
		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);
		radioButton.setOnAction($ -> {
			reportRadioUpdate(radioButtons.getKey(radioButton));
		});
		radioButton.setOnKeyPressed(ke -> {
			for (Listener listener : listeners) {
				listener.keyPressed(ke);
			}
		});
		return radioButton;
	}

	private CheckBox createCheckBox() {
		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction(ae -> {
			reportCompletionUpdate(checkboxes.getKey(checkBox));
		});
		checkBox.setOnKeyPressed(ke -> {
			for (Listener listener : listeners) {
				listener.keyPressed(ke);
			}
		});
		return checkBox;
	}

	private void reportCompletionUpdate(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		final CheckBox checkBox = checkboxes.get(treeItem);
		if (checkBox.isDisabled()) {
			if (treeItem.getChildren().isEmpty()) {
				checkBox.setSelected(false);
			} else {
				final boolean currentState = checkBox.isSelected();
				final boolean selected = treeItem.getChildren().stream().allMatch(ti -> isChecked(ti));
				if (currentState != selected) {
					checkBox.setSelected(selected);
					updateCheckboxMetadata(treeItem, selected);
				}
			}
		} else {
			markDirty();
			updateCheckboxMetadata(treeItem, checkBox.isSelected());
		}
		reportCompletionUpdate(treeItem.getParent());
	}

	private void updateCheckboxMetadata(TreeItem<String> treeItem, boolean selected) {
		if (selected) {
			blacken(treeItem);
			completed.put(treeItem, ZonedDateTime.now());
		} else {
			fillByAge(treeItem);
			completed.remove(treeItem);
		}
	}

	private void markDirty() {
		if (!dirty) {
			dirty = true;
			for (Listener listener : listeners) {
				listener.markDirty();
			}
		}
	}

	private void markClean() {
		if (dirty) {
			dirty = false;
			for (Listener listener : listeners) {
				listener.markClean();
			}
		}
	}

	private void fillByAge(TreeItem<String> treeItem) {
		if (circles.containsKey(treeItem) && created.containsKey(treeItem)) {
			fillByAge(circles.get(treeItem), created.get(treeItem));
		}
	}

	private void blacken(TreeItem<String> treeItem) {
		if (circles.containsKey(treeItem)) {
			circles.get(treeItem).setFill(Color.BLACK);
		}
	}

	private void reportFocusChange() {
		if (focus != null) {
			RadioButton radioButton = radioButtons.get(focus);
			if (radioButton != null) {
				radioButton.setSelected(true);
			}
		}
	}

	private void reportNodeAdded(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		expandNodes(treeItem);
		disableCheckBoxIfChildrenExist(treeItem.getParent());
		reportCompletionUpdate(treeItem.getParent());
	}

	private void reportNodeUpdated(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		disableCheckBoxIfChildrenExist(treeItem);
		reportCompletionUpdate(treeItem);
	}

	private void reportNodeRemoved(TreeItem<String> treeItem, TreeItem<String> parent) {
		if (treeItem == null) {
			return;
		}
		if (isRadioSelected(treeItem)) {
			setRadioSelected(parent, true);
		}
		checkboxes.remove(treeItem);
		completed.remove(treeItem);
		created.remove(treeItem);
		deletes.remove(treeItem);
		radioButtons.remove(treeItem);
		markDirty();
	}

	private void setRadioSelected(TreeItem<String> treeItem, boolean selected) {
		radioButtons.get(treeItem).setSelected(selected);
	}

	private boolean isRadioSelected(TreeItem<String> treeItem) {
		return radioButtons.get(treeItem).isSelected();
	}

	private void reportRadioUpdate(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		focus = treeItem;
	}

	private void disableCheckBoxIfChildrenExist(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		setDisabled(treeItem, !treeItem.getChildren().isEmpty());
		disableCheckBoxIfChildrenExist(treeItem.getParent());
	}

	private void reportTreeChange() {
		focus = tree.getRoot();
		markClean();

		if (focus != null) {
			Deque<TreeItem<String>> deque = new ArrayDeque<>();
			deque.push(focus);

			while (deque.peek() != null) {
				TreeItem<String> node = deque.poll();
				if (node != null) {
					final ObservableList<TreeItem<String>> children = node.getChildren();
					if (children.isEmpty()) {
						// process leaves
						setDisabled(node, false);
					} else {
						// process branches
						node.setExpanded(true);
						setDisabled(node, true);
						deque.addAll(children);
					}
				}
			}
			reportFocusChange();
		}
	}

	private static void expandNodes(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return;
		}
		treeItem.setExpanded(true);
		expandNodes(treeItem.getParent());
	}

	public void save(Stage stage) {
		File selectedFile = fileSaver.showSaveDialog(stage);
		if (selectedFile != null) {
			try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
				toSaveObject(tree.getRoot()).writeTo(fos);
				markClean();
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Problem saving task.");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	private TaskProtos.Task toSaveObject(TreeItem<String> treeItem) {
		List<TaskProtos.Task> children = new ArrayList<>();
		for (TreeItem<String> child : treeItem.getChildren()) {
			children.add(toSaveObject(child));
		}
		final TaskProtos.Task.Builder builder = TaskProtos.Task.newBuilder()
				.addAllChildren(children)
				.setValue(treeItem.getValue())
				.setWhenCreated(fromZDT(created.get(treeItem)));

		if (!isChecked(treeItem) || completed.get(treeItem) == null) {
			return builder.build();
		}
		return builder.setWhenCompleted(fromZDT(completed.get(treeItem))).build();
	}

	private boolean isChecked(TreeItem<String> treeItem) {
		return checkboxes.get(treeItem).isSelected();
	}

	private void setChecked(TreeItem<String> treeItem, boolean selected, ZonedDateTime when) {
		checkboxes.get(treeItem).setSelected(selected);
		if (selected) {
			completed.put(treeItem, checkNotNull(when));
		} else {
			completed.remove(treeItem);
		}
	}

	private void setDisabled(TreeItem<String> treeItem, boolean disabled) {
		checkboxes.get(treeItem).setDisable(disabled);
	}

	public void load(Stage stage) {
		loadNow(fileLoader.showOpenDialog(stage));
	}

	private void loadNow(File selectedFile) {
		if (selectedFile != null) {
			try (FileInputStream fis = new FileInputStream(selectedFile)) {
				TaskProtos.Task task = TaskProtos.Task.parseFrom(fis);
				tree.setRoot(fromSaveObject(task,null));
				reportTreeChange();
				lastLoad = selectedFile;
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Problem loading task.");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	public void reset() {
		if (lastLoad == null) {
			initialize();
		} else {
			loadNow(lastLoad);
		}
		markClean();
	}

	public void clear() {
		lastLoad = null;
		initialize();
		markClean();
	}

	private TreeItem<String> fromSaveObject(TaskProtos.Task task, TreeItem<String> parent) {
		TreeItem<String> treeItem = createTreeItem(parent, task.getValue(), toZDT(task.getWhenCreated()), toZDT(task.getWhenCompleted()));
		for (TaskProtos.Task child : task.getChildrenList()) {
			treeItem.getChildren().add(fromSaveObject(child,treeItem));
		}
		return treeItem;
	}

	public void addLeafAtFocus(String text) {
		checkNotNull(focus);
		final TreeItem<String> ti = createTreeItem(focus, text, ZonedDateTime.now(), null);
		focus.getChildren().add(ti);
		reportNodeAdded(ti);
		markDirty();
	}

	public void moveFocusRight() {
		if (focus != null) {
			if (!focus.getChildren().isEmpty()) {
				focus = focus.getChildren().get(0);
				reportFocusChange();
			}
		}
	}

	public void moveFocusLeft() {
		if (focus != null) {
			final TreeItem<String> parent = focus.getParent();
			if (parent != null) {
				focus = parent;
				reportFocusChange();
			}
		}
	}

	public void moveFocusDown() {
		if (focus != null) {
			final TreeItem<String> parent = focus.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(focus);
				if (pos + 1 < parent.getChildren().size()) {
					focus = parent.getChildren().get(pos + 1);
					reportFocusChange();
				}
			}
		}
	}

	public void moveFocusUp() {
		if (focus != null) {
			final TreeItem<String> parent = focus.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(focus);
				if (pos >= 1) {
					focus = parent.getChildren().get(pos - 1);
					reportFocusChange();
				}
			}
		}
	}

	public static interface Listener {
		public void keyPressed(KeyEvent keyEvent);
		public void markClean();
		public void markDirty();
	}
}
