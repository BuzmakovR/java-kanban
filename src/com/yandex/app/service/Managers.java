package com.yandex.app.service;

import com.yandex.app.service.impl.FileBackedTaskManager;
import com.yandex.app.service.impl.InMemoryHistoryManager;
import com.yandex.app.service.impl.InMemoryTaskManager;
import java.io.File;
import java.io.IOException;

public class Managers {

	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

	public static TaskManager getBackedTaskManager(File file) throws IOException {
		return FileBackedTaskManager.loadFromFile(file);
	}
}
