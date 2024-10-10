package com.yandex.app.service.impl;

import java.util.LinkedList;

import com.yandex.app.model.Task;
import com.yandex.app.service.HistoryManager;

public class InMemoryHistoryManager implements HistoryManager {
	private final LinkedList<Task> history;
	public static final int LIMIT_COUNT = 10;

	public InMemoryHistoryManager(){
		this.history = new LinkedList<>();
	}

	@Override
	public void add(Task task) {
		if (history.size() == LIMIT_COUNT) {
			history.removeFirst();
		}
		history.add(task);
	}
	@Override
	public LinkedList<Task> getHistory() {
		return new LinkedList<>(this.history);
	}
}
