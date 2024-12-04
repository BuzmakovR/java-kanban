package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
	protected int id;
	protected String name;
	protected String description;
	protected TaskStatuses status;
	protected Duration duration;
	protected LocalDateTime startTime;

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

	public Task(String name, String description, TaskStatuses status, LocalDateTime startTime, Duration duration) {
		this.name = name;
		this.description = description;
		this.status = status;
		this.startTime = startTime;
		this.duration = duration;
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

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Duration getDuration() {
		return Optional.ofNullable(duration).orElse(Duration.ofMinutes(0));
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return startTime.plus(getDuration());
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
