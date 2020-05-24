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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TaskManagerApp extends Application {

	private final ToggleGroup toggleGroup = new ToggleGroup();
	private final AtomicReference<TreeItem> focus = new AtomicReference<>();
	private final Map<TreeItem,RadioButton> itemsToRadioButtons = new HashMap<>();

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
		stage.setTitle("Task Manager");
		final Scene scene = new Scene(root, 600, 550);
		stage.setScene(scene);
		stage.show();

		Button btnAddTask = (Button)root.lookup("#btnAddTask");
		TextField txtAddTask = (TextField)root.lookup("#txtAddTask");
		TreeView treeView = (TreeView)root.lookup("#treeView");

		btnAddTask.setOnAction($1 -> {
			final String text = txtAddTask.getText();
			if (text != null && !text.isEmpty()) {
				txtAddTask.clear();
				initializeTreeItem(text);
			}
		});

		txtAddTask.setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.ENTER) {
				final String text = txtAddTask.getText();
				if (text != null && !text.isEmpty()) {
					txtAddTask.clear();
					initializeTreeItem(text);
				}
			}
			registerKeyControls(ke);
		});

		initializeRootItem(treeView);

		scene.setOnKeyPressed(ke -> {
			registerKeyControls(ke);
		});
	}

	private void registerKeyControls(KeyEvent ke) {
		if (ke.isControlDown() && ke.getCode() == KeyCode.RIGHT) {
			System.err.println("Ctrl + Right");
			ke.consume();

			final TreeItem current = focus.get();
			final ObservableList children = current.getChildren();
			if (children != null && !children.isEmpty()) {
				TreeItem next = (TreeItem) children.get(0);
				focus.set(next);
				itemsToRadioButtons.get(next).setSelected(true);
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.LEFT) {
			System.err.println("Ctrl + Left");
			ke.consume();

			final TreeItem current = focus.get();
			final TreeItem parent = current.getParent();
			if (parent != null) {
				focus.set(parent);
				itemsToRadioButtons.get(parent).setSelected(true);
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.DOWN) {
			System.err.println("Ctrl + Down");
			ke.consume();

			final TreeItem current = focus.get();
			final TreeItem parent = current.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(current);
				if (pos + 1 < parent.getChildren().size()) {
					TreeItem next = (TreeItem) parent.getChildren().get(pos + 1);
					focus.set(next);
					itemsToRadioButtons.get(next).setSelected(true);
				}
			}
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.UP) {
			System.err.println("Ctrl + Up");
			ke.consume();

			final TreeItem current = focus.get();
			final TreeItem parent = current.getParent();
			if (parent != null) {
				int pos = parent.getChildren().indexOf(current);
				if (pos >= 1) {
					TreeItem next = (TreeItem) parent.getChildren().get(pos - 1);
					focus.set(next);
					itemsToRadioButtons.get(next).setSelected(true);
				}
			}
		}
	}

	private void hookRadioButton(RadioButton radioButton, TreeItem item) {
		radioButton.setOnAction($2 -> {
			System.err.println("Clicked radio button for '" + item.getValue() + "'");
			focus.set(item);
		});
		radioButton.setOnKeyPressed(ke -> {
			registerKeyControls(ke);
		});
	}

	private RadioButton initializeRadioButton() {
		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);
		return radioButton;
	}

	private void initializeRootItem(TreeView treeView) {
		final CheckBox checkBox = initializeCheckBox();
		final RadioButton radioButton = initializeRadioButton();

		final TreeItem rootItem = new CheckBoxTreeItem("Tasks", new HBox(radioButton,checkBox));
		treeView.setRoot(rootItem);
		focus.set(rootItem);

		hookRadioButton(radioButton, rootItem);
		radioButton.setSelected(true);

		itemsToRadioButtons.put(rootItem,radioButton);
	}

	private void initializeTreeItem(String text) {
		final CheckBox checkBox = initializeCheckBox();
		final RadioButton radioButton = initializeRadioButton();
		final Hyperlink deleteButton = new Hyperlink("Delete");

		final TreeItem item = new CheckBoxTreeItem(text, new HBox(radioButton,checkBox,deleteButton));
		final TreeItem focusedTreeItem = focus.get();
		focusedTreeItem.getChildren().add(item);
		expandTree(focusedTreeItem);
		hookRadioButton(radioButton, item);

		itemsToRadioButtons.put(item,radioButton);

		// delete the item and its children
		deleteButton.setOnAction($2 -> {
			System.err.println("Clicked delete button.");
			focusedTreeItem.getChildren().remove(item);
		});
	}

	private CheckBox initializeCheckBox() {
		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction($2 -> {
			System.err.println("Clicked! Now " + (checkBox.isSelected() ? "selected" : "unselected"));
		});
		return checkBox;
	}

	private static void expandTree(TreeItem item) {
		if (item != null) {
			item.setExpanded(true);

			for (Object child : item.getChildren()) {
				if (child instanceof TreeItem) {
					expandTree((TreeItem)child);
				}
			}
		}
	}
}
