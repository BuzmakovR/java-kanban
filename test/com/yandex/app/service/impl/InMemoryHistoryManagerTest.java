package com.yandex.app.service.impl;
import com.yandex.app.model.Task;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

	@Test
	void historyLengthMatchesLimit() {
		InMemoryHistoryManager hm = new InMemoryHistoryManager();
		final int historyLimit = InMemoryHistoryManager.LIMIT_COUNT;

		for (int i = 1; i <= historyLimit + 10; i++) {
			Task task = new Task("task" + i, "desc" + i);
			task.setId(i);
			hm.add(task);
		}
		List<Task> history = hm.getHistory();
		assertEquals(historyLimit, history.size(), "Длина списка истории не равна ограничению");
	}
	@Test
	void addingItemToEndList() {
		InMemoryHistoryManager hm = new InMemoryHistoryManager();

		Task task1 = new Task("task1", "desc1");
		task1.setId(1);
		hm.add(task1);

		Task task2 = new Task("task2", "desc2");
		task2.setId(2);
		hm.add(task2);

		List<Task> history = hm.getHistory();
		Task lastTask = history.getLast();
		assertEquals(task2, lastTask, "Задачи в историю добавляются не в конец списка");
	}
}
