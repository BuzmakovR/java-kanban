package com.yandex.app.service.impl;

import com.yandex.app.exception.ManagerLoadException;
import com.yandex.app.exception.ManagerSaveException;
import com.yandex.app.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

	private final File file;

	public static FileBackedTaskManager loadFromFile(File file) {
		return new FileBackedTaskManager(file);
	}

	public FileBackedTaskManager(File file) throws ManagerLoadException {
		super();
		this.file = file;
		load();
	}

	protected void save() throws ManagerSaveException {
		List<Task> tasks = getAllItems();

		try (FileWriter fw = new FileWriter(file.getPath(), StandardCharsets.UTF_8, false)) {
			String taskHeader = CsvData.ID.name().toLowerCase() +
					"," + CsvData.TYPE.name().toLowerCase() +
					"," + CsvData.NAME.name().toLowerCase() +
					"," + CsvData.STATUS.name().toLowerCase() +
					"," + CsvData.DESCRIPTION.name().toLowerCase() +
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

	protected void load() throws ManagerLoadException {
		try {
			String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			String[] lines = content.split(System.lineSeparator());

			for (int i = 1; i < lines.length; i++) {
				try {
					Task task = taskFromString(lines[i]);
					if (!addNewItemWithId(task)) {
						System.out.println("[FileBackedTaskManager.load]: Failed to add task to manager: " + task.toString());
					}
				} catch (ManagerLoadException e) {
					System.out.println("[FileBackedTaskManager.load]: " + e.getMessage() + ". Данные: " + lines[i]);
				}
			}
		} catch (IOException e) {
			throw new ManagerLoadException(e.getMessage());
		}
	}

	protected String taskToString(Task task) {
		StringBuilder builder = new StringBuilder(String.valueOf(task.getId()));
		builder.append(",").append(task.getTaskType().toString())
				.append(",").append(task.getName())
				.append(",").append(task.getStatus().toString())
				.append(",").append(task.getDescription())
				.append(",");

		if (task.getTaskType() == TaskTypes.SUBTASK) {
			builder.append(((Subtask) task).getEpicId());
		}
		return builder.append(System.lineSeparator()).toString();
	}

	protected Task taskFromString(String value) throws ManagerLoadException {
		String[] parts = value.split(",");
		Task task;

		try {
			if (CsvData.ID.getIndex() >= parts.length) {
				throw new ManagerLoadException("Failed to get task ID");
			}

			final int id = Integer.parseInt(parts[CsvData.ID.getIndex()].trim());
			final String name = parts.length > CsvData.NAME.getIndex() ? parts[CsvData.NAME.getIndex()] : "";
			final String desc = parts.length > CsvData.DESCRIPTION.getIndex() ? parts[CsvData.DESCRIPTION.getIndex()] : "";
			final TaskStatuses status = parts.length > CsvData.STATUS.getIndex()
					? TaskStatuses.valueOf(parts[CsvData.STATUS.getIndex()]) : null;

			task = switch (TaskTypes.valueOf(parts[CsvData.TYPE.getIndex()])) {
				case TaskTypes.TASK -> new Task(name, desc, status);
				case TaskTypes.SUBTASK -> {
					final int epicId = Integer.parseInt(parts[CsvData.EPIC.getIndex()].trim());
					yield new Subtask(name, desc, status, epicId);
				}
				case TaskTypes.EPIC -> new Epic(name, desc, status);
			};
			task.setId(id);
		} catch (Exception e) {
			throw new ManagerLoadException(e.getMessage());
		}
		return task;
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
		EPIC(5);

		private final int index;

		CsvData(int index) {
			this.index = index;
		}

		int getIndex() {
			return index;
		}
	}
}
