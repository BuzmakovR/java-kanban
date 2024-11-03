package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.List;

public interface TaskManager {

	boolean addNewItem(Task t);

	boolean updateItem(Task t);

	List<Task> getAllItems();

	void deleteAllItems();

	Task getItemById(Integer id);

	void deleteItemById(Integer id);

	List<Task> getTasks();

	void deleteAllTask();

	List<Subtask> getSubtasks();

	void deleteAllSubtask();

	List<Epic> getEpics();

	List<Subtask> getEpicSubtasksById(Integer epicId);

	List<Subtask> getEpicSubtasks(Epic epic);

	void deleteAllEpics();

	List<Task> getHistory();
}
