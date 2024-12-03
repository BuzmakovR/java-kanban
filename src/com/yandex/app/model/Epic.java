package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
	private final List<Integer> subtaskIds;
	private LocalDateTime endTime;

	public Epic(String name, String description) {
		super(name, description);
		this.subtaskIds = new ArrayList<>();
	}

	public Epic(String name, String description, TaskStatuses status) {
		super(name, description, status);
		this.subtaskIds = new ArrayList<>();
	}

	public Epic(String name, String description, TaskStatuses status, List<Integer> subtaskIds) {
		super(name, description);
		this.status = status;
		this.subtaskIds = subtaskIds;
	}

	public Epic(String name, String description, TaskStatuses status, List<Integer> subtaskIds, LocalDateTime startDate,
				Duration duration, LocalDateTime endTime) {
		super(name, description, status, startDate, duration);
		this.subtaskIds = subtaskIds;
		this.endTime = endTime;
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	@Override
	public TaskTypes getTaskType() {
		return TaskTypes.EPIC;
	}

	@Override
	public LocalDateTime getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", status=" + status +
				", subtaskIds=" + subtaskIds.toString() +
				'}';
	}

	public void addSubtaskIds(Integer subtaskId) {
		if (!this.subtaskIds.contains(subtaskId)) this.subtaskIds.add(subtaskId);
	}

	public void deleteSubtaskId(Integer subtaskId) {
		this.subtaskIds.remove(subtaskId);
	}

	public void deleteAllSubtaskIds() {
		this.subtaskIds.clear();
	}
}
