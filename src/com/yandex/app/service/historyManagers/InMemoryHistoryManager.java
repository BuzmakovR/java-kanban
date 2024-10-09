package com.yandex.app.service.historyManagers;

import java.util.ArrayList;
import java.util.List;

import com.yandex.app.model.Task;

public class InMemoryHistoryManager implements HistoryManager{
	private final List<Task> history;
	public static final int LIMIT_COUNT = 10;

	public InMemoryHistoryManager(){
		this.history = new ArrayList<>();
	}

	@Override
	public void add(Task task) {
		if (history.size() == LIMIT_COUNT) {
			history.removeFirst();
		}
		history.add(task);
	}
	@Override
	public List<Task> getHistory() {
		return this.history;
	}
}
