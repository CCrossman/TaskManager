package com.crossman.manager;

import com.sun.source.tree.Tree;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public class TaskManagerApp extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
		stage.setTitle("Task Manager");
		stage.setScene(new Scene(root,300,275));
		stage.show();

		Button btnAddTask = (Button)root.lookup("#btnAddTask");
		TextField txtAddTask = (TextField)root.lookup("#txtAddTask");
		TreeView treeView = (TreeView)root.lookup("#treeView");
		btnAddTask.setOnAction($ -> {
			final String text = txtAddTask.getText();
			if (text != null && !text.isEmpty()) {
				System.err.println("Clicked! Task addition: " + text);
				txtAddTask.clear();

				TreeItem item = new TreeItem(text);
				treeView.getRoot().getChildren().add(item);
			}
		});

		TreeItem rootItem = new TreeItem("Tasks");
		treeView.setRoot(rootItem);
	}
}
