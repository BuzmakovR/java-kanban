package com.yandex.app.service.impl;

import com.yandex.app.exception.ManagerLoadException;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

	static File fileLoad;
	static final String testFileName = "FileBackedTaskManagerTest";
	static final String testFileNameExt = ".csv";

	TaskManager loadTaskManager() {
		try {
			tm = FileBackedTaskManager.loadFromFile(fileLoad);
		} catch (ManagerLoadException e) {
			Assertions.fail(e.getMessage());
		}
		return tm;
	}

	void checkManagerDataAfterLoad(TaskManager tmBeforeLoad) {
		TaskManager tmAfterLoad = loadTaskManager();
		List<Task> tasksBeforeLoad = tmBeforeLoad.getAllItems();
		List<Task> tasksAfterLoad = tmAfterLoad.getAllItems();
		assertEquals(tasksBeforeLoad, tasksAfterLoad, "Список задач после загрузки из файла отличается");

		StringBuilder stringBuilder = new StringBuilder();
		for (Task task : tasksBeforeLoad) {
			stringBuilder.append(task.toString()).append(System.lineSeparator());
		}
		String strTasksBeforeLoad = stringBuilder.toString();

		stringBuilder = new StringBuilder();
		int maxId = 0;
		for (Task task : tasksAfterLoad) {
			stringBuilder.append(task.toString()).append(System.lineSeparator());
			maxId = Math.max(task.getId(), maxId);
		}
		String strTasksAfterLoad = stringBuilder.toString();

		assertEquals(strTasksBeforeLoad, strTasksAfterLoad, "Список задач после загрузки из файла отличается");

		Task task2 = new Task("task2", "task after load");
		tm.addNewItem(task2);
		assertEquals(maxId + 1, task2.getId(), "После загрузки задач nextId некорректный");
	}

	@BeforeEach
	void createFile() {
		try {
			fileLoad = File.createTempFile(testFileName, testFileNameExt);
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	void saveAndLoadEmptyFile() {
		tm = loadTaskManager();
		tm.deleteAllItems();
		checkManagerDataAfterLoad(tm);
	}

	@Test
	void loadTasks() {
		tm = loadTaskManager();
		super.addingNewTask();
		super.addingNewSubtaskAndEpic();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void addingNewTask() {
		tm = loadTaskManager();
		super.addingNewTask();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void addingNewSubtaskAndEpic() {
		tm = loadTaskManager();
		super.addingNewSubtaskAndEpic();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void epicShouldBeEmptyAfterDeletingSubtasks() {
		tm = loadTaskManager();
		super.epicShouldBeEmptyAfterDeletingSubtasks();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void taskDoesntChangeAfterAddingToManager() {
		tm = loadTaskManager();
		super.taskDoesntChangeAfterAddingToManager();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void shouldNotUpdateTaskIfItDoesntExist() {
		tm = loadTaskManager();
		super.shouldNotUpdateTaskIfItDoesntExist();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void fillingHistory() {
		tm = loadTaskManager();
		super.fillingHistory();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void historyStoresPreviousVersionTask() {
		tm = loadTaskManager();
		super.historyStoresPreviousVersionTask();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void managerMustSetNewEpicStatus() {
		tm = loadTaskManager();
		super.managerMustSetNewEpicStatus();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void managerMustSetInProgressEpicStatus() {
		tm = loadTaskManager();
		super.managerMustSetInProgressEpicStatus();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void managerMustSetDoneEpicStatus() {
		tm = loadTaskManager();
		super.managerMustSetDoneEpicStatus();
		checkManagerDataAfterLoad(tm);
	}

	@Override
	@Test
	void testDeletedHistory() {
		tm = loadTaskManager();
		super.testDeletedHistory();
		checkManagerDataAfterLoad(tm);
	}
}
