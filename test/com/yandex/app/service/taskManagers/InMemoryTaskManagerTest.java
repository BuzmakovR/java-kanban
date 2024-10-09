package com.yandex.app.service.taskManagers;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatuses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

	private static InMemoryTaskManager tm;

	@BeforeAll
	static void creatingTaskManager() {
		tm = new InMemoryTaskManager();
		assertNotNull(tm, "Не инициализирован экземпляр класса InMemoryTaskManager");
		assertNotNull(tm.getTasks(), "Не инициализирован список задач в InMemoryTaskManager");
		assertNotNull(tm.getSubtasks(), "Не инициализирован список подзадач в InMemoryTaskManager");
		assertNotNull(tm.getEpics(), "Не инициализирован список эпиков в InMemoryTaskManager");
		assertNotNull(tm.getEpics(), "Не инициализирован список эпиков в InMemoryTaskManager");
		assertNotNull(tm.getHistoryManager(), "Не инициализирован HistoryManager в InMemoryTaskManager");
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
		Subtask subtask1 = new Subtask("SubtaskWithoutEpic", "addingNewSubtaskWithoutEpic");
		assertFalse(tm.addNewItem(subtask1), "Добавлена подзадача без эпика в InMemoryTaskManager");

		Task epic1 = new Epic("Epic1", "addingNewSubtaskAndEpic");
		assertTrue(tm.addNewItem(epic1), "Не удалось добавить эпик в InMemoryTaskManager");
		List<Epic> epics = tm.getEpics();
		assertNotEquals(0, epics.size(), "Список эпиков пуст после добавления в InMemoryTaskManager");
		assertNotNull(tm.getItemById(epic1.getId()), "Не удалось получить эпик после добавления по ID в InMemoryTaskManager");

		subtask1.setEpicId(epic1.getId());
		assertFalse(tm.updateItem(subtask1), "Обновлена подзадача, которой не существует в InMemoryTaskManager");

		Subtask subtask2 = new Subtask("SubtaskWithEpic", "addingNewSubtaskWithEpic");
		subtask2.setEpicId(epic1.getId());
		assertTrue(tm.addNewItem(subtask2), "Не удалось добавить подзадачу в InMemoryTaskManager");
		List<Subtask> subtasks = tm.getSubtasks();
		assertNotEquals(0, subtasks.size(), "Список подзадач пуст после добавления в InMemoryTaskManager");
		assertNotNull(tm.getItemById(subtask2.getId()), "Не удалось получить подзадачу после добавления по ID в InMemoryTaskManager");
	}
	@Test
	void taskDoesntChangeAfterAddingToManager() {
		Task task1 = new Task("TaskNotChange", "taskDoesntChangeAfterAddingToManager");
		tm.addNewItem(task1);
		Task taskFromManager = tm.getItemById(task1.getId());
		assertEquals(task1, taskFromManager, "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager");
		assertEquals(task1.getId(), taskFromManager.getId(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные ID");
		assertEquals(task1.getName(), taskFromManager.getName(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные наименования");
		assertEquals(task1.getDescription(), taskFromManager.getDescription(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные описания");
		assertEquals(task1.getStatus(), taskFromManager.getStatus(), "Задача после добавления в менеджер не соответствует добавляемой в InMemoryTaskManager: разные статусы");
	}
	@Test
	void shouldNotUpdateTaskIfItDoesntExist(){
		Task task = new Task("NotExists", "The task does not exist");
		task.setId(-1);
		assertFalse(tm.updateItem(task), "Обновлена задача, которой не существует в InMemoryTaskManager");
	}
	@Test
	void fillingHistory() {
		Task task = new Task("taskHistoryManager", "historyFilling");
		assertTrue(tm.addNewItem(task), "Не удалось добавить задачу в InMemoryTaskManager");
		task = tm.getItemById(task.getId());

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
		tm.updateItem(updatedTask);

		List<Task> history = tm.getHistory();
		Task taskFromHistory = history.getLast();
		assertNotEquals(updatedTask.getName(), taskFromHistory.getName(), "История задач хранит актуальную версию задачи в InMemoryTaskManager: актуальное наименования");
		assertNotEquals(updatedTask.getDescription(), taskFromHistory.getDescription(), "История задач хранит актуальную версию задачи в InMemoryTaskManager: актуальное описание");
	}
	@Test
	void managerMustSetNewEpicStatus() {
		Task epic = new Epic("EpicStatus", "managerMustSetInProgressEpicStatus");
		tm.addNewItem(epic);
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Новый эпик должен быть в статусе NEW");

		Subtask subtask1 = new Subtask("Subtask1", "Subtask1");
		tm.addNewItem(subtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик с новыми подзадачами должен быть в статусе NEW");
		tm.deleteItemById(subtask1.getId());
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик с новыми подзадачами должен быть в статусе NEW");
		tm.deleteAllSubtask();
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.NEW, "Эпик с новыми подзадачами должен быть в статусе NEW");
	}
	@Test
	void managerMustSetInProgressEpicStatus() {
		Task epic = new Epic("EpicStatus", "managerMustSetInProgressEpicStatus");
		tm.addNewItem(epic);

		// Добавление новой в статусе IN_PROGRESS
		// Статусы подзадач: [IN_PROGRESS]
		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", TaskStatuses.IN_PROGRESS, epic.getId());
		tm.addNewItem(subtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи на тот же
		// Статусы подзадач: [IN_PROGRESS]
		Subtask newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.IN_PROGRESS, subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		tm.updateItem(newVersionSubtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS] должен быть в статусе IN_PROGRESS");

		// Добавление новой подзадачи в статусе NEW
		// Статусы подзадач: [IN_PROGRESS, NEW]
		Subtask subtask2 = new Subtask("Subtask2", "Subtask2");
		subtask2.setEpicId(epic.getId());
		tm.addNewItem(subtask2);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [IN_PROGRESS, NEW] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи DONE
		// Статусы подзадач: [DONE, NEW]
		newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.DONE, subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		tm.updateItem(newVersionSubtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.IN_PROGRESS, "Эпик с подзадачами в статусах [DONE, NEW] должен быть в статусе IN_PROGRESS");

		// Попытка обновления статуса подзадачи IN_PROGRESS
		// Статусы подзадач: [DONE, IN_PROGRESS]
		Subtask newVersionSubtask2 = new Subtask(subtask2.getName(), subtask2.getDescription(), TaskStatuses.IN_PROGRESS, subtask2.getEpicId());
		newVersionSubtask2.setId(subtask2.getId());
		tm.updateItem(newVersionSubtask2);
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
		tm.addNewItem(epic);

		// Добавление новой в статусе DONE
		// Статусы подзадач: [DONE]
		Subtask subtask1 = new Subtask("Subtask1", "Subtask1", TaskStatuses.DONE, epic.getId());
		tm.addNewItem(subtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");

		// Попытка обновления статуса подзадачи на тот же
		// Статусы подзадач: [DONE]
		Subtask newVersionSubtask1 = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.DONE, subtask1.getEpicId());
		newVersionSubtask1.setId(subtask1.getId());
		tm.updateItem(newVersionSubtask1);
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");

		// Добавление новой подзадачи в статусе NEW
		// Статусы подзадач: [DONE, NEW]
		Subtask subtask2 = new Subtask("Subtask2", "Subtask2");
		subtask2.setEpicId(epic.getId());
		tm.addNewItem(subtask2);
		epic = tm.getItemById(epic.getId());
		assertNotEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE, NEW] не сменил статус с DONE");

		// Удаление подзадачи со статусом NEW
		// Статусы подзадач: [DONE]
		tm.deleteItemById(subtask2.getId());
		epic = tm.getItemById(epic.getId());
		assertEquals(epic.getStatus(), TaskStatuses.DONE, "Эпик с подзадачами в статусах [DONE] должен быть в статусе DONE");
	}
}
