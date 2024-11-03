package com.yandex.app.service;

import java.util.List;

import com.yandex.app.model.Task;

public interface HistoryManager {

	void add(Task task);

	List<Task> getHistory();

	void remove(int id);

	void removeAll();
}
