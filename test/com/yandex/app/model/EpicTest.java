package com.yandex.app.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

	@Test
	void epicMustReturnTaskType() {
		Epic epic = new Epic("Epic", "Desciption");
		assertEquals(TaskTypes.EPIC, epic.getTaskType(), "Эпик возвращает тип отличный от EPIC");
	}
	@Test
	void epicsEqualIfIdEqual() {
		Epic epic1 = new Epic("Epic1", "Desciption1");
		epic1.setId(1);

		Epic epic2 = new Epic("Epic2", "Desciption2");
		epic2.setId(1);

		assertEquals(epic1, epic2, "Эпики с одинаковыми ID не равны");
	}
	@Test
	void epicStoresSubtasks() {
		Epic epic1 = new Epic("Epic1", "Desciption1");
		epic1.setId(1);
		Integer subtaskId = 2;
		epic1.addSubtaskIds(subtaskId);
		List<Integer> subtaskIds = epic1.getSubtaskIds();
		assertNotEquals(0, subtaskIds.size(), "Список подзадач в эпике пустой");
		assertTrue(subtaskIds.contains(subtaskId), "Эпик не сохраняет добавленную задачу");
	}
	@Test
	void epicDeleteAllSubtasks() {
		Epic epic1 = new Epic("Epic1", "Desciption1");
		epic1.setId(1);
		Integer subtaskId = 2;
		epic1.addSubtaskIds(subtaskId);
		epic1.deleteAllSubtaskIds();
		List<Integer> subtaskIds = epic1.getSubtaskIds();
		assertEquals(0, subtaskIds.size(), "Список подзадач в эпике не очищается");
	}
	@Test
	void epicDeleteSubtasksById() {
		Epic epic1 = new Epic("Epic1", "Desciption1");
		epic1.setId(1);
		Integer subtaskId = 2;
		epic1.addSubtaskIds(subtaskId);
		epic1.deleteSubtaskId(subtaskId);
		List<Integer> subtaskIds = epic1.getSubtaskIds();
		assertEquals(0, subtaskIds.size(), "Список подзадач не удаляет одно значение");
	}

}