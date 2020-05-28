package com.crossman.manager;

import com.crossman.task.Task;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TaskManagerApp extends Application {
	private final ToggleGroup toggleGroup = new ToggleGroup();
	private final AtomicReference<TreeItem<String>> focus = new AtomicReference<>();
	private final Map<TreeItem<String>,RadioButton> radioButtons = new HashMap<>();
	private final BidiMap<TreeItem<String>,CheckBox> checkboxes = new DualHashBidiMap<>();

	private final FileChooser fileLoader = new FileChooser();
	private final FileChooser fileSaver = new FileChooser();

	public TaskManagerApp() {
		super();
		fileLoader.setTitle("Load Task");
		fileSaver.setTitle("Save Task");
		fileSaver.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tasks", ".tsk"));
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
		stage.setTitle("Task Manager");
		final Scene scene = new Scene(root, 600, 550);
		stage.setScene(scene);
		stage.show();

		Button btnAddTask = (Button)root.lookup("#btnAddTask");
		Button btnSave = (Button)root.lookup("#btnSave");
		Button btnLoad = (Button)root.lookup("#btnLoad");
		TextField txtAddTask = (TextField)root.lookup("#txtAddTask");
		TreeView<String> treeView = (TreeView<String>)root.lookup("#treeView");

		btnAddTask.setOnAction($ -> {
			processTextControls(txtAddTask);
		});

		txtAddTask.setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.ENTER) {
				processTextControls(txtAddTask);
			}
			processKeyControls(ke);
		});

		initializeRootItem(treeView);

		btnSave.setOnAction($ -> {
			processSaveAction(stage,treeView);
		});

		btnLoad.setOnAction($ -> {
			processLoadAction(stage,treeView);
		});

		scene.setOnKeyPressed(ke -> {
			processKeyControls(ke);
		});
	}

	private void processLoadAction(Stage stage, TreeView<String> treeView) {
		System.err.println("Load");
		File selectedFile = fileLoader.showOpenDialog(stage);
		if (selectedFile != null) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
				Task task = (Task)ois.readObject();
				treeView.setRoot(fromSaveObject(task));
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void processSaveAction(Stage stage, TreeView<String> treeView) {
		System.err.println("Save");
		File selectedFile = fileSaver.showSaveDialog(stage);
		if (selectedFile != null) {
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
				oos.writeObject(toSaveObject(treeView.getRoot()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private TreeItem<String> fromSaveObject(Task task) {
		return fromSaveObject(task, null);
	}

	private TreeItem<String> fromSaveObject(Task task, TreeItem<String> parent) {
		TreeItem<String> treeItem = loadTreeItem(parent, task.getValue());
		for (Task child : task.getChildren()) {
			treeItem.getChildren().add(fromSaveObject(child,treeItem));
		}
		expandTree(treeItem);
		return treeItem;
	}

	private static Task toSaveObject(TreeItem<String> treeItem) {
		List<Task> children = new ArrayList<>();
		for (TreeItem<String> child : treeItem.getChildren()) {
			children.add(toSaveObject(child));
		}
		return new Task(treeItem.getValue(), children);
	}

	private void processTextControls(TextField txtAddTask) {
		final String text = txtAddTask.getText();
		if (text != null && !text.isEmpty()) {
			txtAddTask.clear();
			initializeTreeItem(text);
			disableAndCheckParents(focus.get());
		}
	}

	private void processKeyControls(KeyEvent ke) {
		if (ke.isControlDown() && ke.getCode() == KeyCode.RIGHT) {
			//System.err.println("Ctrl + Right");
			ke.consume();

			final TreeItem<String> current = focus.get();
			final ObservableList<TreeItem<String>> children = current.getChildren();
			if (children != null && !children.isEmpty()) {
				TreeItem<String> next = children.get(0);
				focus.set(next);
				radioButtons.get(next).setSelected(true);
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.LEFT) {
			//System.err.println("Ctrl + Left");
			ke.consume();

			final TreeItem<String> current = focus.get();
			final TreeItem<String> parent = current.getParent();
			if (parent != null) {
				focus.set(parent);
				radioButtons.get(parent).setSelected(true);
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.DOWN) {
			//System.err.println("Ctrl + Down");
			ke.consume();

			final TreeItem<String> current = focus.get();
			final TreeItem<String> parent = current.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(current);
				if (pos + 1 < parent.getChildren().size()) {
					TreeItem<String> next = parent.getChildren().get(pos + 1);
					focus.set(next);
					radioButtons.get(next).setSelected(true);
				}
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.UP) {
			//System.err.println("Ctrl + Up");
			ke.consume();

			final TreeItem<String> current = focus.get();
			final TreeItem<String> parent = current.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(current);
				if (pos >= 1) {
					TreeItem<String> next = parent.getChildren().get(pos - 1);
					focus.set(next);
					radioButtons.get(next).setSelected(true);
				}
			}
		}
	}

	private void hookRadioButton(RadioButton radioButton, TreeItem<String> item) {
		radioButtons.put(item,radioButton);

		radioButton.setOnAction($ -> {
			//System.err.println("Clicked radio button for '" + item.getValue() + "'");
			focus.set(item);
		});

		radioButton.setOnKeyPressed(ke -> {
			processKeyControls(ke);
		});
	}

	private RadioButton initializeRadioButton() {
		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);
		return radioButton;
	}

	private void initializeRootItem(TreeView<String> treeView) {
		final CheckBox checkBox = initializeCheckBox();
		final RadioButton radioButton = initializeRadioButton();

		final TreeItem<String> rootItem = new CheckBoxTreeItem<>("Tasks", new HBox(radioButton,checkBox));
		treeView.setRoot(rootItem);
		focus.set(rootItem);

		checkboxes.put(rootItem,checkBox);
		disableAndCheckParents(rootItem);
		hookRadioButton(radioButton, rootItem);
		radioButton.setSelected(true);
	}

	private void initializeTreeItem(String text) {
		final CheckBox checkBox = initializeCheckBox();
		final RadioButton radioButton = initializeRadioButton();
		final Hyperlink deleteButton = new Hyperlink("Delete");

		final TreeItem<String> item = new CheckBoxTreeItem<>(text, new HBox(radioButton,checkBox,deleteButton));
		final TreeItem<String> focusedTreeItem = focus.get();
		focusedTreeItem.getChildren().add(item);
		expandTree(focusedTreeItem);

		checkboxes.put(item,checkBox);
		disableAndCheckParents(item);
		hookRadioButton(radioButton, item);

		// delete the item and its children
		deleteButton.setOnAction($ -> {
			//System.err.println("Clicked delete button.");
			focusedTreeItem.getChildren().remove(item);
			disableAndCheckParents(focusedTreeItem);
		});
	}

	private TreeItem<String> loadTreeItem(TreeItem<String> parent, String text) {
		if (parent == null) {
			final CheckBox checkBox = initializeCheckBox();
			final RadioButton radioButton = initializeRadioButton();

			final TreeItem<String> item = new CheckBoxTreeItem<>(text, new HBox(radioButton, checkBox));

			checkboxes.put(item, checkBox);
			disableAndCheckParents(item);
			hookRadioButton(radioButton, item);

			return item;
		} else {
			final CheckBox checkBox = initializeCheckBox();
			final RadioButton radioButton = initializeRadioButton();
			final Hyperlink deleteButton = new Hyperlink("Delete");

			final TreeItem<String> item = new CheckBoxTreeItem<>(text, new HBox(radioButton, checkBox, deleteButton));

			checkboxes.put(item, checkBox);
			disableAndCheckParents(item);
			hookRadioButton(radioButton, item);

			// delete the item and its children
			deleteButton.setOnAction($ -> {
				//System.err.println("Clicked delete button.");
				parent.getChildren().remove(item);
				disableAndCheckParents(parent);
			});

			return item;
		}
	}

	private void disableAndCheckParents(TreeItem<String> item) {
		if (item == null) {
			return;
		}
		//System.err.println("setCompleteIfChildrenComplete(" + item.getValue() + ")");
		ObservableList<TreeItem<String>> children = item.getChildren();
		CheckBox cb = checkboxes.get(item);
		if (children.isEmpty()) {
			cb.setDisable(false);
			cb.setSelected(false);
			return;
		}
		cb.setDisable(true);
		cb.setSelected(children.stream().allMatch(ti -> checkboxes.get(ti).isSelected()));
		disableAndCheckParents(item.getParent());
	}

	private CheckBox initializeCheckBox() {
		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction($ -> {
			//System.err.println("Clicked! Now " + (checkBox.isSelected() ? "selected" : "unselected"));
			disableAndCheckParents(checkboxes.getKey(checkBox).getParent());
		});
		return checkBox;
	}

	private static void expandTree(TreeItem<String> item) {
		if (item != null) {
			item.setExpanded(true);

			for (TreeItem<String> child : item.getChildren()) {
				expandTree(child);
			}
		}
	}
}
