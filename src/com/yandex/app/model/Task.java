package com.yandex.app.model;

import java.util.Objects;

public class Task {
	protected int id;
	protected String name;
	protected String description;
	protected TaskStatuses status;

	public Task(String name, String description) {
		this.name = name;
		this.description = description;
		this.status = TaskStatuses.NEW;
	}

	public Task(String name, String description, TaskStatuses status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public int getId() {
		return id;
	}
	public boolean setId(int id) {
		if (this.id != 0) {
			return false;
		}
		this.id = id;
		return true;
	}
	public TaskTypes getTaskType() {
		return TaskTypes.TASK;
	}
	public TaskStatuses getStatus() {
		return this.status;
	}
	protected void setStatuses(TaskStatuses status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return getId() == task.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", status=" + status +
				'}';
	}
}
