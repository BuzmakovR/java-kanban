package com.yandex.app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {

	@Test
	void taskMustReturnTaskType() {
		Task task = new Task("Task", "Desciption1");
		assertEquals(TaskTypes.TASK, task.getTaskType(), "Задача возвращает тип отличный от TASK");
	}

	@Test
	void tasksEqualIfIdEqual() {
		Task task1 = new Task("Task1", "Desciption1");
		task1.setId(1);

		Task task2 = new Task("Task2", "Desciption2");
		task2.setId(1);

		assertEquals(task1, task2, "Задачи с одинаковыми ID не равны");
	}
}
