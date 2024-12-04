package com.yandex.app.service.impl;

import com.yandex.app.exception.ManagerLoadException;
import com.yandex.app.exception.ManagerSaveException;
import com.yandex.app.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FileBackedTaskManager extends InMemoryTaskManager {

	private final File file;
	private final DateTimeFormatter dateTimeFormatter;

	public static FileBackedTaskManager loadFromFile(File file) throws IOException {
		return new FileBackedTaskManager(file);
	}

	public FileBackedTaskManager(File file) throws IOException {
		super();
		this.file = file;
		this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		load();
	}

	protected void save() {
		List<Task> tasks = getAllItems();

		try (FileWriter fw = new FileWriter(file.getPath(), StandardCharsets.UTF_8, false)) {
			String taskHeader = CsvData.ID.name().toLowerCase() +
					"," + CsvData.TYPE.name().toLowerCase() +
					"," + CsvData.NAME.name().toLowerCase() +
					"," + CsvData.STATUS.name().toLowerCase() +
					"," + CsvData.DESCRIPTION.name().toLowerCase() +
					"," + CsvData.START_TIME.name().toLowerCase() +
					"," + CsvData.DURATION.name().toLowerCase() +
					"," + CsvData.EPIC.name().toLowerCase() +
					System.lineSeparator();

			fw.write(taskHeader);
			for (Task task : tasks) {
				fw.write(taskToString(task));
			}
		} catch (IOException e) {
			throw new ManagerSaveException(e.getMessage());
		}
	}

	protected void load() throws IOException {
		String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
		String[] lines = content.split(System.lineSeparator());

		for (int i = 1; i < lines.length; i++) {
			Optional<Task> optionalTask = taskFromString(lines[i]);
			if (optionalTask.isPresent()) {
				if (!addNewItemWithId(optionalTask.get()))
					System.out.println("[FileBackedTaskManager.load]: Failed to add task to manager: " + optionalTask.get());
			}
		}
	}

	protected String taskToString(Task task) {
		Function<Long, String> longToStr = num -> Optional.ofNullable(num).map(String::valueOf).orElse("");
		Function<LocalDateTime, String> dateTimeToStr = dateTime -> Optional.ofNullable(dateTime)
				.map(dateTimeFormatter::format)
				.orElse("");
		Function<Duration, String> durationToStr = duration -> Optional.ofNullable(duration)
				.map(d -> longToStr.apply(d.toMinutes()))
				.orElse("");
		StringBuilder builder = new StringBuilder(longToStr.apply((long)task.getId()));
		builder.append(",").append(task.getTaskType().toString())
				.append(",").append(task.getName())
				.append(",").append(task.getStatus().toString())
				.append(",").append(task.getDescription())
				.append(",").append(dateTimeToStr.apply(task.getStartTime()))
				.append(",").append(durationToStr.apply(task.getDuration()))
				.append(",");
		if (task.getTaskType() == TaskTypes.SUBTASK) {
			builder.append(((Subtask) task).getEpicId());
		}
		return builder.append(System.lineSeparator()).toString();
	}

	protected Optional<Task> taskFromString(String value) {
		String[] parts = value.split(",");
		Optional<Task> optionalTask;

		try {
			if (CsvData.ID.getIndex() >= parts.length) {
				throw new ManagerLoadException("Failed to get task ID");
			}
			if (CsvData.TYPE.getIndex() >= parts.length) {
				throw new ManagerLoadException("Failed to get task type");
			}

			final int id = Integer.parseInt(parts[CsvData.ID.getIndex()].trim());
			final String name = parts.length > CsvData.NAME.getIndex() ? parts[CsvData.NAME.getIndex()] : "";
			final String desc = parts.length > CsvData.DESCRIPTION.getIndex() ? parts[CsvData.DESCRIPTION.getIndex()] : "";
			final TaskStatuses status = parts.length > CsvData.STATUS.getIndex()
					? TaskStatuses.valueOf(parts[CsvData.STATUS.getIndex()]) : null;
			final LocalDateTime startDate = parts.length > CsvData.START_TIME.getIndex()
					&& !parts[CsvData.START_TIME.getIndex()].isEmpty()
					? LocalDateTime.parse(parts[CsvData.START_TIME.getIndex()], dateTimeFormatter) : null;
			final Duration duration = parts.length > CsvData.DURATION.getIndex()
					&& !parts[CsvData.DURATION.getIndex()].isEmpty()
					? Duration.ofMinutes(Integer.parseInt(parts[CsvData.DURATION.getIndex()].trim())) : null;

			Task task = switch (TaskTypes.valueOf(parts[CsvData.TYPE.getIndex()])) {
				case TaskTypes.TASK -> new Task(name, desc, status, startDate, duration);
				case TaskTypes.SUBTASK -> {
					final int epicId = Integer.parseInt(parts[CsvData.EPIC.getIndex()].trim());
					yield new Subtask(name, desc, status, startDate, duration, epicId);
				}
				case TaskTypes.EPIC -> new Epic(name, desc, status);
			};
			task.setId(id);
			optionalTask = Optional.of(task);
		} catch (ManagerLoadException e) {
			optionalTask = Optional.empty();
		}
		return optionalTask;
	}

	@Override
	public boolean addNewItem(Task t) {
		boolean added = super.addNewItem(t);
		if (added) save();
		return added;
	}

	@Override
	public boolean updateItem(Task t) {
		boolean updated = super.updateItem(t);
		if (updated) save();
		return updated;
	}

	@Override
	public Task getItemById(Integer id) {
		return super.getItemById(id);
	}

	@Override
	public void deleteItemById(Integer id) {
		super.deleteItemById(id);
		save();
	}

	@Override
	public void deleteAllItems() {
		super.deleteAllItems();
		save();
	}

	@Override
	public void deleteAllTask() {
		super.deleteAllTask();
		save();
	}

	@Override
	public void deleteAllSubtask() {
		super.deleteAllSubtask();
		save();
	}

	@Override
	public void deleteAllEpics() {
		super.deleteAllEpics();
		save();
	}

	private enum CsvData {
		ID(0),
		TYPE(1),
		NAME(2),
		STATUS(3),
		DESCRIPTION(4),
		START_TIME(5),
		DURATION(6),
		EPIC(7);

		private final int index;

		CsvData(int index) {
			this.index = index;
		}

		int getIndex() {
			return index;
		}
	}
}
