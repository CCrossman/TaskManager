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
//	private final ToggleGroup toggleGroup = new ToggleGroup();
//	private final AtomicReference<TreeItem<String>> focus = new AtomicReference<>();
//	private final Map<TreeItem<String>,RadioButton> radioButtons = new HashMap<>();
//	private final BidiMap<TreeItem<String>,CheckBox> checkboxes = new DualHashBidiMap<>();
//
//	private final FileChooser fileLoader = new FileChooser();
//	private final FileChooser fileSaver = new FileChooser();

	private TaskManagerView view;

	public TaskManagerApp() {
		super();
//		fileLoader.setTitle("Load Task");
//		fileSaver.setTitle("Save Task");
//		fileSaver.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tasks", ".tsk"));
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
			//processSaveAction(stage,treeView);
		});

		btnLoad.setOnAction($ -> {
			view.load(stage);
			//processLoadAction(stage,treeView);
		});

		scene.setOnKeyPressed(ke -> {
			processKeyControls(ke);
		});

		view.initialize();
	}

//	private void processLoadAction(Stage stage, TreeView<String> treeView) {
//		System.err.println("Load");
//		File selectedFile = fileLoader.showOpenDialog(stage);
//		if (selectedFile != null) {
//			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
//				Task task = (Task)ois.readObject();
//				treeView.setRoot(fromSaveObject(task));
//			} catch (IOException | ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	private void processSaveAction(Stage stage, TreeView<String> treeView) {
//		System.err.println("Save");
//		File selectedFile = fileSaver.showSaveDialog(stage);
//		if (selectedFile != null) {
//			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
//				oos.writeObject(toSaveObject(treeView.getRoot()));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	private TreeItem<String> fromSaveObject(Task task) {
//		return fromSaveObject(task, null);
//	}
//
//	private TreeItem<String> fromSaveObject(Task task, TreeItem<String> parent) {
//		TreeItem<String> treeItem = loadTreeItem(parent, task.getValue());
//		for (Task child : task.getChildren()) {
//			treeItem.getChildren().add(fromSaveObject(child,treeItem));
//		}
//		expandTree(treeItem);
//		return treeItem;
//	}
//
//	private static Task toSaveObject(TreeItem<String> treeItem) {
//		List<Task> children = new ArrayList<>();
//		for (TreeItem<String> child : treeItem.getChildren()) {
//			children.add(toSaveObject(child));
//		}
//		return new Task(treeItem.getValue(), children);
//	}

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
