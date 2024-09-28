package com.yandex.app.service;

import com.yandex.app.model.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
	private int nextId;
	private final HashMap<Integer, Task> tasks;
	private final HashMap<Integer, Subtask> subtasks;
	private final HashMap<Integer, Epic> epics;

	public TaskManager() {
		nextId = 1;
		tasks = new HashMap<>();
		subtasks = new HashMap<>();
		epics = new HashMap<>();
	}
	public boolean addNewItem(Task t) {
		t.setId(getNextId());
		return addItem(t);
	}
	public boolean updateItem(Task t) {
		return addItem(t);
	}
	public Task getItemById(Integer id) {
		if (tasks.containsKey(id)) {
			return tasks.get(id);
		} else if (subtasks.containsKey(id)) {
			return subtasks.get(id);
		} else if (epics.containsKey(id)) {
			return epics.get(id);
		}
		return null;
	}
	public ArrayList<Task> getAllItems() {
		ArrayList<Task> items = new ArrayList<>(tasks.values());
		items.addAll(subtasks.values());
		items.addAll(epics.values());
		return items;
	}
	public void deleteItemById(Integer id) {
		tasks.remove(id);
		deleteSubtaskById(id);
		deleteEpicById(id);
	}
	public void deleteAllItems(){
		deleteAllTask();
		deleteAllEpics();
		// Удаление всех эпиков удаляет все сабтаски, но на всякий случай вызываем удаление все сабтасков
		deleteAllSubtask();
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
	private int getNextId(){
		return nextId++;
	}
	private boolean addItem(Task t) {
		if (t.getId() == 0) {
			System.out.println("Failed to determine the task ID: " + t);
			return false;
		}
		switch (t.getTaskType()) {
			case TaskTypes.TASK:
				return addTask(t);
			case TaskTypes.SUBTASK:
				return addSubtask((Subtask) t);
			case TaskTypes.EPIC:
				return addEpic((Epic) t);
			default:
				System.out.println("Failed to determine handler for type: " + t.getTaskType());
		}
		return false;
	}

	//region TASK
	public ArrayList<Task> getTasks(){
		return new ArrayList<>(tasks.values());
	}
	public void deleteAllTask() {
		tasks.clear();
	}
	private boolean addTask(Task task) {
		tasks.put(task.getId(), task);
		return true;
	}
	//endregion

	//region SUBTASK
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}
	public void deleteSubtaskById(Integer subtaskId) {
		final Subtask subtask = subtasks.remove(subtaskId);
		if (subtask == null) return;

		final Integer epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		epic.deleteSubtaskId(subtaskId);
		updateEpicStatus(epic);
	}
	public void deleteAllSubtask() {
		if (subtasks.isEmpty()) return;
		subtasks.clear();

		for (Epic epic : epics.values()) {
			epic.deleteAllSubtaskIds();
			updateEpicStatus(epic);
		}
	}
	private boolean addSubtask(Subtask subtask) {
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
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}
	public ArrayList<Subtask> getEpicTasksById(Integer epicId) {
		if (!epics.containsKey(epicId)) {
			return new ArrayList<>();
		}
		return getEpicTasks(epics.get(epicId));
	}
	public ArrayList<Subtask> getEpicTasks(Epic epic) {
		ArrayList<Subtask> subtasks = new ArrayList<>();
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.add(this.subtasks.get(subtaskId));
		}
		return subtasks;
	}
	public void deleteEpicById(Integer id) {
		final Epic epic = epics.remove(id);
		if (epic == null) return;

		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
		}
	}
	public void deleteAllEpics() {
		if (epics.isEmpty()) return;

		subtasks.clear();
		epics.clear();
	}
	private boolean addEpic(Epic epic) {
		epics.put(epic.getId(), epic);
		return true;
	}
	private void updateEpicStatus(Epic epic) {
		TaskStatuses newStatus = TaskStatuses.NEW;
		ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

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
		Epic newEpic = new Epic(epic.getName(), epic.getDescription(), newStatus, epic.getSubtaskIds());
		newEpic.setId(epic.getId());
		updateItem(newEpic);
	}
	//endregion

}
