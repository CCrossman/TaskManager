package com.crossman.manager;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TaskManagerApp extends Application {
	private final ToggleGroup toggleGroup = new ToggleGroup();
	private final AtomicReference<TreeItem<String>> focus = new AtomicReference<>();
	private final Map<TreeItem<String>,RadioButton> radioButtons = new HashMap<>();
	private final BidiMap<TreeItem<String>,CheckBox> checkboxes = new DualHashBidiMap<>();

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
		stage.setTitle("Task Manager");
		final Scene scene = new Scene(root, 600, 550);
		stage.setScene(scene);
		stage.show();

		Button btnAddTask = (Button)root.lookup("#btnAddTask");
		TextField txtAddTask = (TextField)root.lookup("#txtAddTask");
		TreeView<String> treeView = (TreeView<String>)root.lookup("#treeView");

		btnAddTask.setOnAction($1 -> {
			processTextControls(txtAddTask);
		});

		txtAddTask.setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.ENTER) {
				processTextControls(txtAddTask);
			}
			processKeyControls(ke);
		});

		initializeRootItem(treeView);

		scene.setOnKeyPressed(ke -> {
			processKeyControls(ke);
		});
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

		radioButton.setOnAction($2 -> {
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
		deleteButton.setOnAction($2 -> {
			//System.err.println("Clicked delete button.");
			focusedTreeItem.getChildren().remove(item);
			disableAndCheckParents(focusedTreeItem);
		});
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
		checkBox.setOnAction($2 -> {
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
