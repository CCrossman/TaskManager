package com.crossman.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
		final Scene scene = new Scene(root, 305, 390);
		stage.setScene(scene);
		stage.show();

		Button btnAddTask = (Button)root.lookup("#btnAddTask");

		final MenuBar menuBar = (MenuBar) root.getChildrenUnmodifiable().stream().filter(n -> n instanceof MenuBar).findFirst().get();

		final MenuButton mFile = (MenuButton) menuBar.lookup("#mFile");
		for (MenuItem mi : mFile.getItems()) {
			switch (mi.getId()) {
				case "miLoad" -> mi.setOnAction($ -> {
					loadNow(stage);
				});
				case "miSave" -> mi.setOnAction($ -> {
					view.save(stage);
				});
			}
		}

		final MenuButton mEdit = (MenuButton) menuBar.lookup("#mEdit");
		for (MenuItem mi : mEdit.getItems()) {
			switch (mi.getId()) {
				case "miClear" -> mi.setOnAction($ -> {
					view.clear();
				});
				case "miReset" -> mi.setOnAction($ -> {
					view.reset();
				});
			}
		}

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

	private void loadNow(Stage stage) {
		if (view.isDirty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING, "You should save your changes.");
			alert.showAndWait();
		} else {
			view.load(stage);
		}
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
