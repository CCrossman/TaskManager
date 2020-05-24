package com.crossman.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class TaskManagerApp extends Application {

	private final ToggleGroup toggleGroup = new ToggleGroup();
	private final AtomicReference<TreeItem> focus = new AtomicReference<>();

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
		stage.setTitle("Task Manager");
		stage.setScene(new Scene(root,600,550));
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
		});

		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction($2 -> {
			System.err.println("Clicked! Now " + (checkBox.isSelected() ? "selected" : "unselected"));
		});

		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);

		TreeItem rootItem = new CheckBoxTreeItem("Tasks", new HBox(radioButton,checkBox));
		treeView.setRoot(rootItem);
		focus.set(rootItem);

		// mark a tree item as the focus
		radioButton.setOnAction($2 -> {
			System.err.println("Clicked root radio button.");
			focus.set(rootItem);
		});
		radioButton.setSelected(true);
	}

	private void initializeTreeItem(String text) {
		System.err.println("Clicked! Task addition: " + text);

		final CheckBox checkBox = new CheckBox();
		checkBox.setOnAction($2 -> {
			System.err.println("Clicked! Now " + (checkBox.isSelected() ? "selected" : "unselected"));
		});

		final RadioButton radioButton = new RadioButton();
		radioButton.setToggleGroup(toggleGroup);

		final Hyperlink deleteButton = new Hyperlink("Delete");

		final TreeItem item = new CheckBoxTreeItem(text, new HBox(radioButton,checkBox,deleteButton));
		final TreeItem focusedTreeItem = focus.get();
		focusedTreeItem.getChildren().add(item);
		expandTree(focusedTreeItem);

		// mark a tree item as the focus
		radioButton.setOnAction($2 -> {
			System.err.println("Clicked radio button.");
			focus.set(item);
		});

		// delete the item and its children
		deleteButton.setOnAction($2 -> {
			System.err.println("Clicked delete button.");
			focusedTreeItem.getChildren().remove(item);
		});
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
