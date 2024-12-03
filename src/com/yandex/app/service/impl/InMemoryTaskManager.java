package com.yandex.app.service.impl;

import com.yandex.app.model.*;
import com.yandex.app.service.Managers;
import com.yandex.app.service.HistoryManager;
import com.yandex.app.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
	private int nextId;
	protected final Map<Integer, Task> tasks;
	protected final Map<Integer, Subtask> subtasks;
	protected final Map<Integer, Epic> epics;
	protected final TreeSet<Task> prioritizedTasks;
	protected final HistoryManager historyManager;

	public InMemoryTaskManager() {
		nextId = 1;
		tasks = new HashMap<>();
		subtasks = new HashMap<>();
		epics = new HashMap<>();
		prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
		historyManager = Managers.getDefaultHistory();
	}

	protected boolean addNewItemWithId(Task t) {
		if (taskIdIsEmpty(t)) {
			return false;
		}
		setCorrectNextId(t.getId());
		return addItem(t, false);
	}

	protected boolean tasksOverlapInTime(Task task1, Task task2) {
		if (task1.getStartTime() == null || task2.getStartTime() == null) {
			return false;
		}
		return task1.getStartTime().isEqual(task2.getStartTime()) ||
				(task2.getStartTime().isAfter(task1.getStartTime()) && task2.getStartTime().isBefore(task1.getEndTime())) ||
				(task1.getStartTime().isAfter(task2.getStartTime()) && task1.getStartTime().isBefore(task2.getEndTime()));
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
		items.addAll(epics.values());
		items.addAll(subtasks.values());
		return items;
	}

	@Override
	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	@Override
	public void deleteItemById(Integer id) {
		Optional.ofNullable(tasks.remove(id)).ifPresent(task -> {
			prioritizedTasks.remove(task);
			historyManager.remove(task.getId());
		});
		deleteSubtaskById(id);
		deleteEpicById(id);
	}

	@Override
	public void deleteAllItems() {
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

	private int getNextId() {
		return nextId++;
	}

	private void setCorrectNextId(int id) {
		if (id >= nextId) {
			nextId = ++id;
		}
	}

	private boolean taskIdIsEmpty(Task t) {
		if (t.getId() == 0) {
			System.out.println("Failed to determine the task ID: " + t);
			return true;
		}
		return false;
	}

	private boolean addItem(Task t, boolean update) {
		if (taskIdIsEmpty(t)) {
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
	public List<Task> getTasks() {
		return new ArrayList<>(tasks.values());
	}

	@Override
	public void deleteAllTask() {
		tasks.values().forEach(task -> {
			prioritizedTasks.remove(task);
			historyManager.remove(task.getId());
		});
		tasks.clear();
	}

	private boolean addTask(Task task, boolean update) {
		if (update && !tasks.containsKey(task.getId())) {
			System.out.println("Failed to get task by ID: " + task.getId());
			return false;
		}
		if (task.getStartTime() != null) {
			if (prioritizedTasks.stream()
					.filter(anyTask -> !anyTask.equals(task))
					.anyMatch(anyTask -> tasksOverlapInTime(anyTask, task))) {
				System.out.println("The tasks overlap in time");
				return false;
			}
			prioritizedTasks.remove(task);
			prioritizedTasks.add(task);
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

		subtasks.values().forEach(subtask -> {
			prioritizedTasks.remove(subtask);
			historyManager.remove(subtask.getId());
		});
		subtasks.clear();

		epics.values().forEach(epic -> {
			epic.deleteAllSubtaskIds();
			updateEpicDataBySubtasks(epic);
		});
	}

	private void deleteSubtaskById(Integer subtaskId) {
		Optional.ofNullable(subtasks.remove(subtaskId)).ifPresent(subtask -> {
			historyManager.remove(subtask.getId());

			Optional.ofNullable(subtask.getStartTime()).ifPresent(localDateTime -> prioritizedTasks.remove(subtask));
			Optional.ofNullable(epics.get(subtask.getEpicId())).ifPresent(epic -> {
				epic.deleteSubtaskId(subtask.getId());
				updateEpicDataBySubtasks(epic);
			});
		});
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
		if (subtask.getStartTime() != null) {
			if (prioritizedTasks.stream()
					.filter(anyTask -> !anyTask.equals(subtask))
					.anyMatch(anyTask -> tasksOverlapInTime(anyTask, subtask))) {
				System.out.println("The subtasks overlap in time");
				return false;
			}
			prioritizedTasks.add(subtask);
		}
		subtasks.put(subtask.getId(), subtask);

		Epic epic = epics.get(subtask.getEpicId());
		epic.addSubtaskIds(subtask.getId());
		updateEpicDataBySubtasks(epic);

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
		return Optional.ofNullable(epics.get(epicId))
				.map(this::getEpicSubtasks)
				.orElse(List.of());
	}

	@Override
	public List<Subtask> getEpicSubtasks(Epic epic) {
		return epic.getSubtaskIds().stream()
				.map(this.subtasks::get)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteAllEpics() {
		if (epics.isEmpty()) return;

		subtasks.values().forEach(subtask -> {
			prioritizedTasks.remove(subtask);
			historyManager.remove(subtask.getId());
		});
		subtasks.clear();

		epics.keySet().forEach(historyManager::remove);
		epics.clear();
	}

	private void deleteEpicById(Integer id) {
		Optional.ofNullable(epics.remove(id)).ifPresent(epic -> {
			historyManager.remove(epic.getId());
			epic.getSubtaskIds().forEach(subtaskId -> {
				Optional.ofNullable(subtasks.remove(subtaskId)).ifPresent(subtask ->
						Optional.ofNullable(subtask.getStartTime())
								.ifPresent(localDateTime -> prioritizedTasks.remove(subtask))
				);
				historyManager.remove(subtaskId);
			});
		});
	}

	private boolean addEpic(Epic epic, boolean update) {
		if (update && !epics.containsKey(epic.getId())) {
			System.out.println("Failed to get epic by ID: " + epic.getId());
			return false;
		}
		epics.put(epic.getId(), epic);
		return true;
	}

	private void updateEpicDataBySubtasks(Epic epic) {
		TaskStatuses newStatus = calculateEpicStatus(epic);
		Optional<LocalDateTime> optionalStartDate = epic.getSubtaskIds().stream()
				.map(subtaskId -> this.subtasks.get(subtaskId).getStartTime())
				.filter(startTime -> Optional.ofNullable(startTime).isPresent())
				.min(Comparator.naturalOrder());
		LocalDateTime startDate = optionalStartDate.orElse(null);

		Duration duration = epic.getSubtaskIds().stream()
				.map(subtaskId -> this.subtasks.get(subtaskId).getDuration())
				.reduce(Duration.ZERO, Duration::plus);

		Optional<Subtask> lastSubtask = Optional.empty();
		if (startDate != null) {
			lastSubtask = epic.getSubtaskIds().stream()
					.map(this.subtasks::get)
					.filter(subtask -> Optional.ofNullable(subtask.getStartTime()).isPresent())
					.max(Comparator.comparing(Subtask::getStartTime));
		}
		Epic newEpic = new Epic(epic.getName(), epic.getDescription(), newStatus, epic.getSubtaskIds(),
				startDate,
				(duration.toMinutes() > 0 ? duration : null),
				(lastSubtask.map(Task::getEndTime).orElse(null)));
		newEpic.setId(epic.getId());
		updateItem(newEpic);
	}

	private TaskStatuses calculateEpicStatus(Epic epic) {
		TaskStatuses newStatus = TaskStatuses.NEW;

		boolean existsInProgress = false;
		boolean existsNotNew = false;
		boolean allDone = !epic.getSubtaskIds().isEmpty();

		for (Integer subtaskId : epic.getSubtaskIds()) {
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
		return newStatus;
	}
	//endregion

}
