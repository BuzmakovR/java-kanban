package com.yandex.app.model;

import java.util.ArrayList;

public class Epic extends Task {
	private final ArrayList<Integer> subtaskIds;

	public Epic(String name, String description) {
		super(name, description, TaskStatuses.NEW);
		subtaskIds = new ArrayList<>();
	}
	public Epic(String name, String description, TaskStatuses status) {
		super(name, description, status);
		subtaskIds = new ArrayList<>();
	}
	public Epic(String name, String description, TaskStatuses status, ArrayList<Integer> subtaskIds) {
		super(name, description, status);
		this.subtaskIds = subtaskIds;
	}
	public ArrayList<Integer> getSubtaskIds() {
		return subtaskIds;
	}
	@Override
	public TaskTypes getTaskType() {
		return TaskTypes.EPIC;
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
