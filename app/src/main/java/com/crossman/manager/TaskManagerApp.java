package com.crossman.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class TaskManagerApp extends Application {
	private TaskManagerView view;

	public TaskManagerApp() {
		super();
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
		Button btnReset = (Button)root.lookup("#btnReset");
		Button btnClear = (Button)root.lookup("#btnClear");
		TextField txtAddTask = (TextField)root.lookup("#txtAddTask");
		TreeView<String> treeView = (TreeView<String>)root.lookup("#treeView");
		this.view = new TaskManagerView(treeView);

		btnAddTask.setOnAction($ -> {
			processTextControls(txtAddTask);
		});

		txtAddTask.setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.ENTER) {
				processTextControls(txtAddTask);
			}
			processKeyControls(ke);
		});

		btnSave.setOnAction($ -> {
			view.save(stage);
		});

		btnLoad.setOnAction($ -> {
			if (view.isDirty()) {
				Alert alert = new Alert(Alert.AlertType.WARNING, "You should save your changes.");
				alert.showAndWait();
			} else {
				view.load(stage);
			}
		});

		btnReset.setOnAction($ -> {
			view.reset();
		});

		btnClear.setOnAction($ -> {
			view.clear();
		});

		scene.setOnKeyPressed(ke -> {
			processKeyControls(ke);
		});

		view.addListener(new TaskManagerView.Listener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				processKeyControls(keyEvent);
			}

			@Override
			public void markClean() {
				stage.setTitle("Task Manager");
			}

			@Override
			public void markDirty() {
				stage.setTitle("Task Manager [dirty]");
			}
		});

		view.initialize();
	}

	private void processTextControls(TextField txtAddTask) {
		final String text = txtAddTask.getText();
		if (text != null && !text.isEmpty()) {
			txtAddTask.clear();
			view.addLeafAtFocus(text);
		}
	}

	private void processKeyControls(KeyEvent ke) {
		if (ke.isControlDown() && ke.getCode() == KeyCode.RIGHT) {
			ke.consume();
			view.moveFocusRight();
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.LEFT) {
			ke.consume();
			view.moveFocusLeft();
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.DOWN) {
			ke.consume();
			view.moveFocusDown();
		}
		if (ke.isControlDown() && ke.getCode() == KeyCode.UP) {
			ke.consume();
			view.moveFocusUp();
		}
	}
}
