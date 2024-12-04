package com.yandex.app;

import com.yandex.app.exception.ManagerLoadException;
import com.yandex.app.model.*;
import com.yandex.app.service.TaskManager;
import com.yandex.app.service.impl.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		System.out.println("Поехали!");

		File file = new File("./data/taskManagerData.csv");
		TaskManager tm;
		try {
			tm = FileBackedTaskManager.loadFromFile(file);
		} catch (ManagerLoadException e) {
			System.out.println(e.getMessage());
			return;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		printAllTasks(tm);

		Task task2 = new Task("task2", "task2-description2");
		tm.addNewItem(task2);

		printAllTasks(tm);
	}

	private static void printAllTasks(TaskManager manager) {
		System.out.println("Задачи:");
		for (Task task : manager.getTasks()) {
			System.out.println(task);
		}
		System.out.println("Эпики:");
		for (Task task : manager.getEpics()) {
			System.out.println(task);
			Epic epic = (Epic) task;
			for (Task subtask : manager.getEpicSubtasks(epic)) {
				System.out.println("--> " + subtask);
			}
		}
		System.out.println("Подзадачи:");
		for (Task subtask : manager.getSubtasks()) {
			System.out.println(subtask);
		}

		System.out.println("История:");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}

		System.out.println("-----------------------------");
	}
}
