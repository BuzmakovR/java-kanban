package com.yandex.app.service.historyManagers;
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
}
