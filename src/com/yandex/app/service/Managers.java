package com.yandex.app.service;

import com.yandex.app.service.impl.InMemoryHistoryManager;
import com.yandex.app.service.impl.InMemoryTaskManager;

public class Managers {

	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}
	
	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

}
