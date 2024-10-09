package com.yandex.app.service.historyManagers;

import java.util.List;

import com.yandex.app.model.Task;

public interface HistoryManager {

	public void add(Task task);

	public List<Task> getHistory();

}
