package com.yandex.app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {

	@Test
	void subtaskMustReturnTaskType() {
		Subtask subtask = new Subtask("Subtask", "Desciption1", 0);
		assertEquals(TaskTypes.SUBTASK, subtask.getTaskType(), "Подзадача возвращает тип отличный от SUBTASK");
	}

	@Test
	void tasksEqualIfIdEqual() {
		Subtask subtask1 = new Subtask("Subtask1", "Desciption1", 0);
		subtask1.setId(1);

		Subtask subtask2 = new Subtask("Subtask2", "Desciption2", 0);
		subtask2.setId(1);

		assertEquals(subtask1, subtask2, "Подзадачи с одинаковыми ID не равны");
	}
}
