package com.yandex.app.service.impl;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatuses;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

	protected static TaskManager tm;

	void checkTaskManagerInit() {
		assertNotNull(tm, "Не инициализирован экземпляр класса TaskManager");
		assertNotNull(tm.getTasks(), "Не инициализирован список задач в TaskManager");
		assertNotNull(tm.getSubtasks(), "Не инициализирован список подзадач в TaskManager");
		assertNotNull(tm.getEpics(), "Не инициализирован список эпиков в TaskManager");
		assertNotNull(tm.getHistoryManager(), "Не инициализирован HistoryManager в TaskManager");
		assertNotNull(tm.getPrioritizedTasks(), "Не инициализирован getPrioritizedTasks в TaskManager");
	}

	@BeforeEach
	void creatingTaskManager() {
		tm = new InMemoryTaskManager();
		checkTaskManagerInit();
	}

	@Test
	void addingNewTask() {
		Task task1 = new Task("Task1", "addingNewTask");
		assertTrue(tm.addNewItem(task1), "Не удалось добавить задачу в InMemoryTaskManager");
		List<Task> tasks = tm.getTasks();
		assertNotEquals(0, tasks.size(), "Список задач пуст после добавления в InMemoryTaskManager");
		assertNotNull(tm.getItemById(task1.getId()), "Не удалось получить задачу по ID после добавления  в InMemoryTaskManager");
	}

	@Test
	void addingNewSubtaskAndEpic() {

		Task epic1 = new Epic("Epic1", "addingNewSubtaskAndEpic");
		assertTrue(tm.addNewItem(epic1), "Не удалось добавить эпик в InMemoryTaskManager");
		List<Epic> epics = tm.getEpics();
		assertNotEquals(0, epics.size(), "Список эпиков пуст после добавления в InMemoryTaskManager");
		assertNotNull(tm.getItemById(epic1.getId()), "Не удалось получить эпик после добавления по ID в InMemoryTaskManager");

		Subtask subtask2 = new Subtask("SubtaskWithEpic", "addingNewSubtaskWithEpic", epic1.getId());
		subtask2.setEpicId(epic1.getId());
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		List<Subtask> subtasks = tm.getSubtasks();
		assertNotEquals(0, subtasks.size(), "Список подзадач пуст после добавления в InMemoryTaskManager");
		assertNotNull(tm.getItemById(subtask2.getId()), "Не удалось получить подзадачу после добавления по ID в InMemoryTaskManager");
	}

	@Test
	void epicShouldBeEmptyAfterDeletingSubtasks() {
		Task epic = new Epic("EpicEmpty", "epicShouldBeEmptyAfterDeletingSubtasks");
		assertTrue(tm.addNewItem(epic), "Не удалось добавить эпик в InMemoryTaskManager");

		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", TaskStatuses.NEW, LocalDateTime.now(), Duration.ofMinutes(60), epic.getId());
		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		List<Subtask> subtasks = tm.getEpicSubtasksById(epic.getId());
		assertEquals(1, subtasks.size(), "Список подзадач пуст");
		tm.deleteItemById(subtask1.getId());
		subtasks = tm.getEpicSubtasksById(epic.getId());
		assertEquals(0, subtasks.size(), "Список подзадач эпика не пустой после удаления подзадач");
	}

	@Test
	void taskDoesntChangeAfterAddingToManager() {
		Task task1 = new Task("TaskNotChange", "taskDoesntChangeAfterAddingToManager");
		assertTrue(tm.addNewItem(task1), "Не удалось добавить задачу в InMemoryTaskManager");
		Task taskFromManager = tm.getItemById(task1.getId());
		assertEquals(task1, taskFromManager, "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager");
		assertEquals(task1.getId(), taskFromManager.getId(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные ID");
		assertEquals(task1.getName(), taskFromManager.getName(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные наименования");
		assertEquals(task1.getDescription(), taskFromManager.getDescription(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные описания");
		assertEquals(task1.getStatus(), taskFromManager.getStatus(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные статусы");
	}

	@Test
	void shouldNotUpdateTaskIfItDoesntExist() {
		Task task = new Task("NotExists", "The task does not exist");
		task.setId(-1);
		assertFalse(tm.updateItem(task), "Обновлена задача, которой не существует в InMemoryTaskManager");
	}

	@Test
	void fillingHistory() {
		Task task = new Task("taskHistoryManager", "historyFilling");
		assertTrue(tm.addNewItem(task), "Не удалось добавить задачу в InMemoryTaskManager");
		tm.getItemById(task.getId());

		List<Task> history = tm.getHistory();
		assertNotEquals(0, history.size(), "Список истории не заполняется в InMemoryTaskManager");
	}

	@Test
	void historyStoresPreviousVersionTask() {
		Task task = new Task("taskHistoryManager", "historyStoresPreviousVersionTask");
		assertTrue(tm.addNewItem(task), "Не удалось добавить задачу в InMemoryTaskManager");

		task = tm.getItemById(task.getId());
		Task updatedTask = new Task(task.getName() + "Updated", "New version");
		updatedTask.setId(task.getId());
		assertTrue(tm.updateItem(updatedTask), "Не удалось обновить задачу в InMemoryTaskManager");

		List<Task> history = tm.getHistory();
		Task taskFromHistory = history.getLast();
		assertNotEquals(updatedTask.getName(), taskFromHistory.getName(), "История задач хранит актуальную версию задачи в InMemoryTaskManager: актуальное наименования");
		assertNotEquals(updatedTask.getDescription(), taskFromHistory.getDescription(), "История задач хранит актуальную версию задачи в InMemoryTaskManager: актуальное описание");
	}

	@Test
	void managerMustSetNewEpicStatus() {
		Task epic = new Epic("EpicStatus", "managerMustSetInProgressEpicStatus");
		assertTrue(tm.addNewItem(epic), "Не удалось добавить эпик в InMemoryTaskManager");
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Новый эпик должен быть в статусе NEW");

		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", epic.getId());
		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик с новыми подзадачами должен быть в статусе NEW");
		tm.deleteItemById(subtask1.getId());
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик без подзадач должен быть в статусе NEW");
		tm.deleteAllSubtask();
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик без подзадач должен быть в статусе NEW");
	}

	@Test
	void managerMustSetInProgressEpicStatus() {
		Task epic = new Epic("EpicStatus", "managerMustSetInProgressEpicStatus");
		tm.addNewItem(epic);

		// Добавление новой в статусе IN_PROGRESS
		// Статусы подзадач: [IN_PROGRESS]
		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", TaskStatuses.IN_PROGRESS,
				LocalDateTime.now(), Duration.ofMinutes(60), epic.getId());
		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи на тот же
		// Статусы подзадач: [IN_PROGRESS]
		Subtask newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.IN_PROGRESS,
				subtask1.getStartTime(), subtask1.getDuration(), subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		assertTrue(tm.updateItem(newVersionSubtask1), "Не удалось обновить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS] должен быть в статусе IN_PROGRESS");

		// Добавление новой подзадачи в статусе NEW
		// Статусы подзадач: [IN_PROGRESS, NEW]
		Subtask subtask2 = new Subtask("Subtask2", "Subtask2", epic.getId());
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS, NEW] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи DONE
		// Статусы подзадач: [DONE, NEW]
		newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.DONE,
				subtask1.getStartTime(), subtask1.getDuration(), subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		assertTrue(tm.updateItem(newVersionSubtask1), "Не удалось обновить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [DONE, NEW] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи IN_PROGRESS
		// Статусы подзадач: [DONE, IN_PROGRESS]
		Subtask newVersionSubtask2 = new Subtask(subtask2.getName(), subtask2.getDescription(), TaskStatuses.IN_PROGRESS,
				subtask2.getStartTime(), subtask2.getDuration(), subtask2.getEpicId());
		newVersionSubtask2.setId(subtask2.getId());
		assertTrue(tm.updateItem(newVersionSubtask2), "Не удалось обновить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [DONE, IN_PROGRESS] должен быть в статусе IN_PROGRESS");

		// Удаление подзадачи со статусом DONE
		// Статусы подзадач: [IN_PROGRESS]
		tm.deleteItemById(newVersionSubtask1.getId());
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS] должен быть в статусе IN_PROGRESS");
	}

	@Test
	void managerMustSetDoneEpicStatus() {
		Task epic = new Epic("EpicStatus", "managerMustSetDoneEpicStatus");
		assertTrue(tm.addNewItem(epic), "Не добавить эпик в InMemoryTaskManager");

		// Добавление новой в статусе DONE
		// Статусы подзадач: [DONE]
		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", TaskStatuses.DONE,
				LocalDateTime.now(), Duration.ofMinutes(60), epic.getId());
		assertTrue(tm.addNewItem(subtask1), "Не добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");

		// Попытка обновления статуса подзадачи на тот же
		// Статусы подзадач: [DONE]
		Subtask newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.DONE,
				subtask1.getStartTime(), subtask1.getDuration(), subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		assertTrue(tm.updateItem(newVersionSubtask1), "Не удалось обновить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");

		// Добавление новой подзадачи в статусе NEW
		// Статусы подзадач: [DONE, NEW]
		Subtask subtask2 = new Subtask("Subtask2", "Subtask2", epic.getId());
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		epic = tm.getItemById(epic.getId());
		assertNotEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE, NEW] не сменил статус с DONE");

		// Удаление подзадачи со статусом NEW
		// Статусы подзадач: [DONE]
		tm.deleteItemById(subtask2.getId());
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");
	}

	@Test
	void testDeletedHistory() {
		// Кейс пользовательского сценария (6 спринт)

		Task task1 = new Task("task1", "task1-description1");
		assertTrue(tm.addNewItem(task1), "Не удалось добавить задачу в InMemoryTaskManager");
		Task task2 = new Task("task2", "task2-description2");
		assertTrue(tm.addNewItem(task2), "Не удалось добавить задачу в InMemoryTaskManager");

		Epic epic3tasks = new Epic("epic3tasks", "epic with 3 tasks");
		assertTrue(tm.addNewItem(epic3tasks), "Не удалось добавить эпик в InMemoryTaskManager");

		Subtask subtask1 = new Subtask("subtask1", "subtask1-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask2 = new Subtask("subtask2", "subtask2-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask3 = new Subtask("subtask3", "subtask3-description", TaskStatuses.NEW, epic3tasks.getId());

		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в InMemoryTaskManager");
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		assertTrue(tm.addNewItem(subtask3), "Не удалось добавить подзадачу в InMemoryTaskManager");

		Epic epicEmpty = new Epic("epicEmpty", "epic is empty");
		assertTrue(tm.addNewItem(epicEmpty), "Не удалось добавить эпик в InMemoryTaskManager");

		List<Task> history = tm.getHistory();
		assertEquals(0, history.size(), "После создания история не пустая");

		tm.getItemById(task1.getId());
		tm.getItemById(epicEmpty.getId());
		tm.getItemById(subtask3.getId());
		tm.getItemById(subtask2.getId());
		tm.getItemById(subtask1.getId());
		tm.getItemById(epic3tasks.getId());
		tm.getItemById(task2.getId());

		history = tm.getHistory();
		assertEquals(7, history.size(), "После обращения к 7 элементам история должна отображать 7 записей");

		tm.getItemById(task1.getId());
		tm.getItemById(task2.getId());
		tm.getItemById(epicEmpty.getId());
		tm.getItemById(epic3tasks.getId());
		tm.getItemById(subtask1.getId());
		tm.getItemById(subtask2.getId());
		tm.getItemById(subtask3.getId());

		history = tm.getHistory();
		assertEquals(7, history.size(), "После повторного обращения к тем же 7 элементам история должна отображать 7 записей без дублей");

		tm.deleteItemById(task2.getId());
		history = tm.getHistory();
		assertEquals(6, history.size(), "История не должна содержать удаленную задачу");

		tm.deleteItemById(subtask2.getId());
		history = tm.getHistory();
		assertEquals(5, history.size(), "История не должна содержать удаленную подзадачу");

		tm.deleteItemById(epic3tasks.getId());
		history = tm.getHistory();
		assertEquals(2, history.size(), "История не должна содержать удаленный эпик и его подзадачи");
	}

	@Test
	void testTasksOverlapInTime() {
		Task firstTask = new Task("task1", "task StartDate: 01:00 | Duration: 0 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 0), Duration.ofMinutes(0));
		assertTrue(tm.addNewItem(firstTask), "Не удалось добавить задачу в InMemoryTaskManager");
		assertFalse(tm.getPrioritizedTasks().isEmpty(), "Список задач по приоритету пустой");

		// Новая задача начинается в то же время с продолжительностью 0
		Task task2 = new Task("task2", "task StartDate: 01:00 | Duration: 0 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 0), Duration.ofMinutes(0));
		assertFalse(tm.addNewItem(task2), "Добавлена задача с пересечением по дате начала");
		assertEquals(1, tm.getAllItems().size(), "Список задач должен содержать только 1 корректно заведенную задачу");

		// Новая задача начинается в то же время с продолжительностью 60
		Task task3 = new Task("task3", "task StartDate: 01:00 | Duration: 60 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 0), Duration.ofMinutes(60));
		assertFalse(tm.addNewItem(task3), "Добавлена задача с пересечением по дате начала");
		assertEquals(1, tm.getAllItems().size(), "Список задач должен содержать только 1 корректно заведенную задачу");

		// Обновляем время выполнения firstTask: Задача начинается в то же время с продолжительностью 60
		Task updateTask = new Task(firstTask.getName(), "task StartDate: 01:00 | Duration: 60 min", firstTask.getStatus(), firstTask.getStartTime(), Duration.ofMinutes(60));
		updateTask.setId(firstTask.getId());
		assertTrue(tm.updateItem(updateTask), "Не удалось обновить продолжительность задачи");

		// Новая задача начинается во время выполнения firstTask с продолжительностью 0
		Task task4 = new Task("task4", "task StartDate: 01:30 | Duration: 0 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 30), Duration.ofMinutes(0));
		assertFalse(tm.addNewItem(task4), "Добавлена задача с пересечением во времени выполнения");
		assertEquals(1, tm.getAllItems().size(), "Список задач должен содержать только 1 корректно заведенную задачу");

		// Новая задача начинается во время выполнения firstTask с продолжительностью 30 (до времени завершения firstTask)
		Task task5 = new Task("task5", "task StartDate: 01:30 | Duration: 30 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 30), Duration.ofMinutes(30));
		assertFalse(tm.addNewItem(task5), "Добавлена задача с пересечением во времени выполнения");
		assertEquals(1, tm.getAllItems().size(), "Список задач должен содержать только 1 корректно заведенную задачу");

		// Новая задача заканчивается во время выполнения firstTask с продолжительностью 60
		Task task6 = new Task("task6", "task StartDate: 00:30 | Duration: 60 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 0, 30), Duration.ofMinutes(60));
		assertFalse(tm.addNewItem(task6), "Добавлена задача с пересечением во времени выполнения");
		assertEquals(1, tm.getAllItems().size(), "Список задач должен содержать только 1 корректно заведенную задачу");

		// Новая задача начинается до начала выполнения firstTask с продолжительностью 30 (task7.endDate == firstTask.startDate)
		Task task7 = new Task("task7", "task StartDate: 00:30 | Duration: 30 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 0, 30), Duration.ofMinutes(30));
		assertTrue(tm.addNewItem(task7), "Не добавлена задача, которая заканчивается по времени с началом первой");
		assertEquals(2, tm.getAllItems().size(), "Список задач должен содержать 2 корректно заведенные задачи");

		// Новая задача начинается после выполнения firstTask с продолжительностью 30 (task8.startDate == firstTask.endDate)
		Task task8 = new Task("task8", "task StartDate: 00:30 | Duration: 30 min", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 0, 0), Duration.ofMinutes(30));
		assertTrue(tm.addNewItem(task8), "Не добавлена задача, которая заканчивается по времени с началом первой");
		assertEquals(3, tm.getAllItems().size(), "Список задач должен содержать 3 корректно заведенные задачи");

		Epic epic = new Epic("epicWithTask", "epicWithTask");
		assertTrue(tm.addNewItem(epic), "Не удалось добавить новый эпик");
		assertEquals(0, epic.getDuration().toMinutes(), "Продолжительность эпика не пустая");
		assertNull(epic.getEndTime(), "Дата окончания эпика не пустая");

		// Subtasks - добавляем. Продолжительность Epic с 05:00 до 05:30
		Subtask subtask1 = new Subtask("subtask1", "subtask StartDate: 05:00 | Duration: 30 min",
				TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 5, 0),
				Duration.ofMinutes(30),
				epic.getId());
		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в эпик");

		// Subtasks - добавляем. Продолжительность Epic с 05:00 до 06:00
		Subtask subtask2 = new Subtask("subtask2", "subtask StartDate: 05:30 | Duration: 30 min",
				TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 5, 30),
				Duration.ofMinutes(30),
				epic.getId());
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в эпик");

		// Subtasks - добавляем с пересечением времени выполнения, проверяем валидацию. Продолжительность Epic с 05:00 до 06:00
		Subtask subtask3 = new Subtask("subtask3", "subtask StartDate: 05:15 | Duration: 15 min",
				TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 5, 15),
				Duration.ofMinutes(15),
				epic.getId());
		assertFalse(tm.addNewItem(subtask3), "Добавлена задача с пересечением во времени выполнения");

		// Продолжительность Epic с 05:00 до 06:00
		epic = (Epic) tm.getItemById(epic.getId());
		assertEquals(60, epic.getDuration().toMinutes(), "Продолжительность эпика пустая");
		assertEquals(LocalDateTime.of(2020, 1, 1, 6, 0), epic.getEndTime(), "Дата окончания некорректная");
	}

	@Test
	void testDeleteMethods() {
		Task task1 = new Task("task2", "task2-description2");
		assertTrue(tm.addNewItem(task1), "Не удалось добавить задачу в InMemoryTaskManager");
		tm.deleteAllTask();
		assertEquals(0, tm.getTasks().size(), "После удаления список задач не пустой");

		Task task2 = new Task("task2", "task2-description2");
		assertTrue(tm.addNewItem(task2), "Не удалось добавить задачу в InMemoryTaskManager");
		tm.deleteItemById(task2.getId());
		assertEquals(0, tm.getTasks().size(), "После удаления список задач не пустой");

		Epic epic3tasks = new Epic("epic3tasks", "epic with 3 tasks");
		assertTrue(tm.addNewItem(epic3tasks), "Не удалось добавить эпик в InMemoryTaskManager");

		Subtask subtask1 = new Subtask("subtask1", "subtask1-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask2 = new Subtask("subtask2", "subtask2-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask3 = new Subtask("subtask3", "subtask3-description", TaskStatuses.NEW, epic3tasks.getId());

		assertTrue(tm.addNewItem(subtask1), "Не удалось добавить подзадачу в InMemoryTaskManager");
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		assertTrue(tm.addNewItem(subtask3), "Не удалось добавить подзадачу в InMemoryTaskManager");
		tm.deleteAllSubtask();
		assertEquals(0, tm.getSubtasks().size(), "После удаления список подзадач не пустой");

		tm.deleteAllEpics();
		assertEquals(0, tm.getEpics().size(), "После удаления список эпиков не пустой");

		Epic epicEmpty = new Epic("epicEmpty", "epic is empty");
		assertTrue(tm.addNewItem(epicEmpty), "Не удалось добавить эпик в InMemoryTaskManager");
		tm.deleteAllItems();
		assertEquals(0, tm.getAllItems().size(), "После удаления всех элементов список не пустой");
	}
}
