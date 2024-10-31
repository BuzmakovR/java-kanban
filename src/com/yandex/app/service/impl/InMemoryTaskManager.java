package com.yandex.app.service.impl;

import com.yandex.app.model.*;
import com.yandex.app.service.Managers;
import com.yandex.app.service.HistoryManager;
import com.yandex.app.service.TaskManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
	private int nextId;
	private final Map<Integer, Task> tasks;
	private final Map<Integer, Subtask> subtasks;
	private final Map<Integer, Epic> epics;
	private final HistoryManager historyManager;

	public InMemoryTaskManager() {
		nextId = 1;
		tasks = new HashMap<>();
		subtasks = new HashMap<>();
		epics = new HashMap<>();
		historyManager = Managers.getDefaultHistory();
	}
	@Override
	public boolean addNewItem(Task t) {
		if (!t.setId(getNextId())) {
			System.out.println("Could not add new item because the ID was defined previously. ID: " + t.getId());
			return false;
		}
		return addItem(t, false);
	}
	@Override
	public boolean updateItem(Task t) {
		return addItem(t, true);
	}
	@Override
	public Task getItemById(Integer id) {
		Task task = null;
		if (tasks.containsKey(id)) {
			task = tasks.get(id);
		} else if (subtasks.containsKey(id)) {
			task = subtasks.get(id);
		} else if (epics.containsKey(id)) {
			task = epics.get(id);
		}
		if (task != null) addItemToHistory(task);

		return task;
	}
	@Override
	public List<Task> getAllItems() {
		List<Task> items = new ArrayList<>(tasks.values());
		items.addAll(subtasks.values());
		items.addAll(epics.values());
		return items;
	}
	@Override
	public void deleteItemById(Integer id) {
		tasks.remove(id);
		deleteSubtaskById(id);
		deleteEpicById(id);
		historyManager.remove(id);
	}
	@Override
	public void deleteAllItems(){
		deleteAllTask();
		deleteAllEpics();
		// Удаление всех эпиков удаляет все сабтаски, но на всякий случай вызываем удаление все сабтасков
		deleteAllSubtask();
		historyManager.removeAll();
	}
	@Override
	public String toString() {
		return "TaskManager{" +
				"nextId=" + nextId +
				", tasks=" + tasks +
				", subtasks=" + subtasks +
				", epics=" + epics +
				'}';
	}
	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}
	public HistoryManager getHistoryManager() {
		return historyManager;
	}
	private void addItemToHistory(Task task) {
		historyManager.add(task);
	}
	private int getNextId(){
		return nextId++;
	}
	private boolean addItem(Task t, boolean update) {
		if (t.getId() == 0) {
			System.out.println("Failed to determine the task ID: " + t);
			return false;
		}
		switch (t.getTaskType()) {
			case TaskTypes.TASK:
				return addTask(t, update);
			case TaskTypes.SUBTASK:
				return addSubtask((Subtask) t, update);
			case TaskTypes.EPIC:
				return addEpic((Epic) t, update);
			default:
				System.out.println("Failed to determine handler for type: " + t.getTaskType());
		}
		return false;
	}
	//region TASK
	@Override
	public List<Task> getTasks(){
		return new ArrayList<>(tasks.values());
	}
	@Override
	public void deleteAllTask() {
		tasks.clear();
	}
	private boolean addTask(Task task, boolean update) {
		if (update && !tasks.containsKey(task.getId())) {
			System.out.println("Failed to get task by ID: " + task.getId());
			return false;
		}
		tasks.put(task.getId(), task);
		return true;
	}
	//endregion

	//region SUBTASK
	@Override
	public List<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}
	@Override
	public void deleteAllSubtask() {
		if (subtasks.isEmpty()) return;
		subtasks.clear();

		for (Epic epic : epics.values()) {
			epic.deleteAllSubtaskIds();
			updateEpicStatus(epic);
		}
	}
	private void deleteSubtaskById(Integer subtaskId) {
		final Subtask subtask = subtasks.remove(subtaskId);
		if (subtask == null) return;

		final Integer epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		epic.deleteSubtaskId(subtaskId);
		updateEpicStatus(epic);
	}
	private boolean addSubtask(Subtask subtask, boolean update) {
		if (update && !subtasks.containsKey(subtask.getId())) {
			System.out.println("Failed to get subtask by ID: " + subtask.getId());
			return false;
		}
		int epicId = subtask.getEpicId();
		if (epicId == 0) {
			System.out.println("EpicId is empty for subtask: " + subtask);
			return false;
		}
		if (!epics.containsKey(epicId)) {
			System.out.println("Failed to get epic by Id: " + epicId + " Subtask: " + subtask);
			return false;
		}
		subtasks.put(subtask.getId(), subtask);

		Epic epic = epics.get(subtask.getEpicId());
		epic.addSubtaskIds(subtask.getId());
		updateEpicStatus(epic);

		return true;
	}
	//endregion

	//region EPIC
	@Override
	public List<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}
	@Override
	public List<Subtask> getEpicSubtasksById(Integer epicId) {
		if (!epics.containsKey(epicId)) {
			return new ArrayList<>();
		}
		return getEpicSubtasks(epics.get(epicId));
	}
	@Override
	public List<Subtask> getEpicSubtasks(Epic epic) {
		List<Subtask> subtasks = new ArrayList<>();
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.add(this.subtasks.get(subtaskId));
		}
		return subtasks;
	}
	@Override
	public void deleteAllEpics() {
		if (epics.isEmpty()) return;

		subtasks.clear();
		epics.clear();
	}
	private void deleteEpicById(Integer id) {
		final Epic epic = epics.remove(id);
		if (epic == null) return;

		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
			historyManager.remove(subtaskId);
		}
	}
	private boolean addEpic(Epic epic, boolean update) {
		if (update && !epics.containsKey(epic.getId())) {
			System.out.println("Failed to get epic by ID: " + epic.getId());
			return false;
		}
		epics.put(epic.getId(), epic);
		return true;
	}
	private void updateEpicStatus(Epic epic) {
		TaskStatuses newStatus = TaskStatuses.NEW;
		List<Integer> subtaskIds = epic.getSubtaskIds();

		if (!subtaskIds.isEmpty()) {
			boolean existsInProgress = false;
			boolean existsNotNew = false;
			boolean allDone = true;

			for (Integer subtaskId : subtaskIds) {
				Subtask st = subtasks.get(subtaskId);

				TaskStatuses status = st.getStatus();
				allDone = allDone && (status == TaskStatuses.DONE);

				if (status == TaskStatuses.IN_PROGRESS) {
					existsInProgress = true;
					break;
				}
				existsNotNew = existsNotNew || (status != TaskStatuses.NEW);
			}
			if (allDone) {
				newStatus = TaskStatuses.DONE;
			} else if (existsInProgress || existsNotNew) {
				newStatus = TaskStatuses.IN_PROGRESS;
			}
		}
		if (epic.getStatus() != newStatus) {
			Epic newEpic = new Epic(epic.getName(), epic.getDescription(), newStatus, epic.getSubtaskIds());
			newEpic.setId(epic.getId());
			updateItem(newEpic);
		}
	}
	//endregion

}
