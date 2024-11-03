package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;

public class Main {

	public static void main(String[] args) {
		System.out.println("Поехали!");

		TaskManager tm = Managers.getDefault();
		Task task1 = new Task("task1", "task1-description1");
		tm.addNewItem(task1);
		Task task2 = new Task("task2", "task2-description2");
		tm.addNewItem(task2);

		Epic epic3tasks = new Epic("epic3tasks", "epic with 3 tasks");
		tm.addNewItem(epic3tasks);
		Subtask subtask1 = new Subtask("subtask1", "subtask1-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask2 = new Subtask("subtask2", "subtask2-description", TaskStatuses.NEW, epic3tasks.getId());
		Subtask subtask3 = new Subtask("subtask3", "subtask3-description", TaskStatuses.NEW, epic3tasks.getId());
		tm.addNewItem(subtask1);
		tm.addNewItem(subtask2);
		tm.addNewItem(subtask3);

		Epic epicEmpty = new Epic("epicEmpty", "epic is empty");
		tm.addNewItem(epicEmpty);

		printAllTasks(tm);

		tm.getItemById(task1.getId());
		tm.getItemById(epicEmpty.getId());
		tm.getItemById(subtask3.getId());
		tm.getItemById(subtask2.getId());
		tm.getItemById(subtask1.getId());
		tm.getItemById(epic3tasks.getId());
		tm.getItemById(task2.getId());

		tm.getItemById(task1.getId());
		tm.getItemById(task2.getId());
		tm.getItemById(epicEmpty.getId());
		tm.getItemById(epic3tasks.getId());
		tm.getItemById(subtask1.getId());
		tm.getItemById(subtask2.getId());
		tm.getItemById(subtask3.getId());

		printAllTasks(tm);

		tm.deleteItemById(task2.getId());

		printAllTasks(tm);

		tm.deleteItemById(epic3tasks.getId());

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
