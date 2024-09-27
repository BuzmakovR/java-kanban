package taskManager;

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

	public int getNextId(){
		return nextId++;
	}
	public boolean addItem(Task t) {
		t.setId(getNextId());
		return _addItem(t);
	}
	public boolean updateItem(Task t) {
		return _addItem(t);
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
	public HashMap<Integer, Task> getAllItems() {
		HashMap<Integer, Task> items = new HashMap<>();
		for (Integer id : tasks.keySet()) {
			items.put(id, tasks.get(id));
		}
		for (Integer id : subtasks.keySet()) {
			items.put(id, subtasks.get(id));
		}
		for (Integer id : epics.keySet()) {
			items.put(id, epics.get(id));
		}
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

	private boolean _addItem(Task t) {
		if (t.getId() == 0) {
			System.out.println("Failed to determine the task ID: " + t);
			return false;
		}
		switch (t.getTaskType()) {
			case TaskTypes.TASK:
				return _addTask(t);
			case TaskTypes.SUBTASK:
				return _addSubtask((Subtask) t);
			case TaskTypes.EPIC:
				return _addEpic((Epic) t);
			default:
				System.out.println("Failed to determine handler for type: " + t.getTaskType());
		}
		return false;
	}

	//region TASK
	private boolean _addTask(Task task) {
		tasks.put(task.getId(), task);
		return true;
	}
	public HashMap<Integer, Task> getTasks(){
		return tasks;
	}
	public void deleteAllTask() {
		tasks.clear();
	}
	//endregion

	//region SUBTASK
	private boolean _addSubtask(Subtask subtask) {
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
	public HashMap<Integer, Subtask> getSubtasks() {
		return subtasks;
	}
	public void deleteSubtaskById(Integer subtaskId) {
		if (!subtasks.containsKey(subtaskId)) return;

		Subtask subtask = subtasks.get(subtaskId);
		Integer epicId = subtask.getEpicId();
		subtasks.remove(subtaskId);

		if (!epics.containsKey(epicId)) return;

		Epic epic = epics.get(epicId);
		epic.deleteSubtaskId(subtaskId);
		updateEpicStatus(epic);
	}
	public void deleteAllSubtask() {
		if (subtasks.isEmpty()) return;

		ArrayList<Epic> epicsForUpdate = new ArrayList<>();

		for (Subtask st : subtasks.values()) {
			Integer epicId = st.getEpicId();
			if (epics.containsKey(epicId)) {
				Epic epic = epics.get(epicId);
				epic.deleteSubtaskId(st.getId());
				epicsForUpdate.add(epics.get(epicId));
			}
		}
		subtasks.clear();

		for (Epic epic : epicsForUpdate) {
			updateEpicStatus(epic);
		}
	}
	//endregion

	//region EPIC
	private boolean _addEpic(Epic epic) {
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
				if (st == null) continue;

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
		epic.setStatuses(newStatus);
	}
	public HashMap<Integer, Epic> getEpics() {
		return epics;
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
			if (!this.subtasks.containsKey(subtaskId)) continue;
			subtasks.add(this.subtasks.get(subtaskId));
		}
		return subtasks;
	}
	public void deleteEpicById(Integer id) {
		if (!epics.containsKey(id)) return;

		Epic epic = epics.get(id);
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
		}
		epics.remove(id);
	}
	public void deleteAllEpics() {
		if (epics.isEmpty()) return;

		for (Epic epic : epics.values()) {
			for (Integer subtaskId : epic.getSubtaskIds()) {
				subtasks.remove(subtaskId);
			}
		}
		epics.clear();
	}
	//endregion

	@Override
	public String toString() {
		return "TaskManager{" +
				"nextId=" + nextId +
				", tasks=" + tasks +
				", subtasks=" + subtasks +
				", epics=" + epics +
				'}';
	}
}
