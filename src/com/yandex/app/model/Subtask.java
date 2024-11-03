package com.yandex.app.model;

public class Subtask extends Task {
	private int epicId;

	public Subtask(String name, String description) {
		super(name, description, TaskStatuses.NEW);
	}

	public Subtask(String name, String description, TaskStatuses status) {
		super(name, description, status);
	}

	public Subtask(String name, String description, TaskStatuses status, int epicId) {
		super(name, description, status);
		this.epicId = epicId;
	}

	public int getEpicId() {
		return epicId;
	}

	public boolean setEpicId(int epicId) {
		if (this.epicId != 0) {
			return false;
		}
		this.epicId = epicId;
		return true;
	}

	@Override
	public TaskTypes getTaskType() {
		return TaskTypes.SUBTASK;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", status=" + status +
				", epicId=" + epicId +
				'}';
	}
}
