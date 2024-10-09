package com.yandex.app.service;

import com.yandex.app.service.taskManagers.*;
import com.yandex.app.service.historyManagers.*;

public class Managers {

	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}
	
	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

}
