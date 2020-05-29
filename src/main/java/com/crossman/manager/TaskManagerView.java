package com.crossman.manager;

import com.crossman.task.Task;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.*;
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

	private TreeItem<String> focus;

	public TaskManagerView(TreeView<String> treeView) {
		this.tree = treeView;
		this.focus = treeView.getRoot();

		fileLoader.setTitle("Open Task");

		fileSaver.setTitle("Save Task");
		fileSaver.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task Files", "*.tsk"));
	}

	public void initialize() {
		final TreeItem<String> treeItem = createTreeItem(null, "Tasks");
		tree.setRoot(treeItem);
		reportTreeChange();
	}

	private TreeItem<String> createTreeItem(TreeItem<String> parent, String label) {
		final CheckBox checkBox = createCheckBox();
		final RadioButton radioButton = createRadioButton();

		final CheckBoxTreeItem<String> treeItem;
		if (parent == null) {
			treeItem = new CheckBoxTreeItem<>(label, new HBox(radioButton, checkBox));
		} else {
			final Hyperlink hyperlink = createDeleteLink();
			treeItem = new CheckBoxTreeItem<>(label, new HBox(radioButton, checkBox, hyperlink));
			deletes.put(treeItem, hyperlink);
		}

		checkboxes.put(treeItem, checkBox);
		radioButtons.put(treeItem, radioButton);
		return treeItem;
	}

	private Hyperlink createDeleteLink() {
		final Hyperlink hyperlink = new Hyperlink("Delete");
		hyperlink.setOnAction($ -> {
			System.err.println("Delete button pressed.");
			final TreeItem<String> deletee = deletes.getKey(hyperlink);
			final TreeItem<String> parent = deletee.getParent();
			if (parent != null) {
				parent.getChildren().remove(deletee);
				reportNodeRemoved(deletee,parent);
				reportNodeUpdated(parent);
			}
		});
		return hyperlink;
	}

	private RadioButton createRadioButton() {
		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);
		radioButton.setOnAction($ -> {
			System.err.println("Radio button chosen.");
			reportRadioUpdate(radioButtons.getKey(radioButton));
		});
		return radioButton;
	}

	private CheckBox createCheckBox() {
		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction($ -> {
			System.err.println("Clicked! Now " + (checkBox.isSelected() ? "selected" : "unselected"));
			reportCompletionUpdate(checkboxes.getKey(checkBox));
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
				checkBox.setSelected(treeItem.getChildren().stream().allMatch(ti -> isChecked(ti)));
			}
		}
		reportCompletionUpdate(treeItem.getParent());
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
		deletes.remove(treeItem);
		radioButtons.remove(treeItem);
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
		System.err.println("Save");
		File selectedFile = fileSaver.showSaveDialog(stage);
		if (selectedFile != null) {
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
				oos.writeObject(toSaveObject(tree.getRoot()));
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Problem saving task.");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	private Task toSaveObject(TreeItem<String> treeItem) {
		List<Task> children = new ArrayList<>();
		for (TreeItem<String> child : treeItem.getChildren()) {
			children.add(toSaveObject(child));
		}
		return new Task(treeItem.getValue(), children, isChecked(treeItem));
	}

	private boolean isChecked(TreeItem<String> treeItem) {
		return checkboxes.get(treeItem).isSelected();
	}

	private void setChecked(TreeItem<String> treeItem, boolean selected) {
		checkboxes.get(treeItem).setSelected(selected);
	}

	private void setDisabled(TreeItem<String> treeItem, boolean disabled) {
		checkboxes.get(treeItem).setDisable(disabled);
	}

	public void load(Stage stage) {
		System.err.println("Load");
		File selectedFile = fileLoader.showOpenDialog(stage);
		if (selectedFile != null) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
				Task task = (Task)ois.readObject();
				tree.setRoot(fromSaveObject(task,null));
				reportTreeChange();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Problem loading task.");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	private TreeItem<String> fromSaveObject(Task task, TreeItem<String> parent) {
		TreeItem<String> treeItem = createTreeItem(parent, task.getValue());
		for (Task child : task.getChildren()) {
			treeItem.getChildren().add(fromSaveObject(child,treeItem));
		}
		if (task.isCompleted()) {
			setChecked(treeItem, true);
		}
		return treeItem;
	}

	public void addLeafAtFocus(String text) {
		checkNotNull(focus);
		final TreeItem<String> ti = createTreeItem(focus, text);
		focus.getChildren().add(ti);
		reportNodeAdded(ti);
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

	}
}
