package com.yandex.app.service.impl;
import com.yandex.app.model.Task;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

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

	@Test
	void removeDuplicateAndAddValueToEnd() {
		InMemoryHistoryManager hm = new InMemoryHistoryManager();
		Task task1 = new Task("task1", "desc1");
		task1.setId(1);
		hm.add(task1);

		Task task2 = new Task("task2", "desc2");
		task2.setId(2);
		hm.add(task2);

		Task task3 = new Task("task3", "desc3");
		task3.setId(3);
		hm.add(task3);

		hm.add(task1);

		List<Task> history = hm.getHistory();
		Task lastTask = history.getLast();
		assertEquals(3, history.size(), "Длина списка истории сохраняет дубли");
		assertEquals(task1, lastTask, "Повторное добавление задачи в историю не сохраняет задачу в конец списка");
	}

	@Test
	void removeItemFromHistory() {
		InMemoryHistoryManager hm = new InMemoryHistoryManager();
		Task task1 = new Task("task1", "desc1");
		task1.setId(1);
		hm.add(task1);

		Task task2 = new Task("task2", "desc2");
		task2.setId(2);
		hm.add(task2);

		Task task3 = new Task("task3", "desc3");
		task3.setId(3);
		hm.add(task3);

		Task task4 = new Task("task4", "desc4");
		task4.setId(4);
		hm.add(task4);

		hm.remove(task2.getId());
		List<Task> history = hm.getHistory();
		assertEquals(3, history.size(), "После удаление из середины списка длина списка истории некорректная");
		for (Task t : history) {
			assertNotEquals(task2, t, "Задача не удаляется из середины истории");
		}

		hm.remove(task1.getId());
		history = hm.getHistory();
		assertEquals(2, history.size(), "После удаление из начала списка длина списка истории некорректная");
		for (Task t : history) {
			assertNotEquals(task1, t, "Задача не удаляется из начала истории");
		}

		hm.remove(task4.getId());
		history = hm.getHistory();
		assertEquals(1, history.size(), "После удаление из конца списка длина списка истории некорректная");
		for (Task t : history) {
			assertNotEquals(task4, t, "Задача не удаляется из конца истории");
		}

		hm.removeAll();
		history = hm.getHistory();
		assertEquals(0, history.size(), "После удаление всех записей из истории длина списка истории некорректная");
	}
}
