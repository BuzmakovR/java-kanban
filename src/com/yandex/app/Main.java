package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		System.out.println("Поехали!");

		TaskManager tm = new TaskManager();
		tm.addNewItem(new Task("task1", "task1-description1"));

		Epic epic = new Epic("epic1", "epic1-description1");
		tm.addNewItem(epic);
		tm.addNewItem(new Subtask("subtask1", "subtask1-description1", TaskStatuses.NEW, epic.getId()));

		Subtask subtask2 = new Subtask("subtask2", "subtask2-description1");
		subtask2.setEpicId(epic.getId());
		tm.addNewItem(subtask2);

		System.out.println(tm);

		Task tempTask = tm.getItemById(3);
		if (tempTask != null && tempTask.getTaskType() == TaskTypes.SUBTASK) {
			Subtask subtask1 = (Subtask) tempTask;
			Subtask subtask1Updated = new Subtask(subtask1.getName(), subtask1.getDescription(), TaskStatuses.DONE, subtask1.getEpicId());
			subtask1Updated.setId(subtask1.getId());
			tm.updateItem(subtask1Updated);
		}

		System.out.println(tm);

		ArrayList<Task> tasks = tm.getTasks();
		ArrayList<Subtask> subtasks = tm.getSubtasks();
		ArrayList<Epic> epics = tm.getEpics();
		ArrayList<Subtask> epicSubtasks = tm.getEpicTasksById(epic.getId());

		ArrayList<Task> allItems = tm.getAllItems();

		tm.deleteItemById(4);
		System.out.println(tm);

		Subtask subtask3 = new Subtask("subtask3", "subtask2-description3");
		subtask3.setEpicId(epic.getId());
		tm.addNewItem(subtask3);

		System.out.println(tm);

		ArrayList<Subtask> epicSubtasks2 = tm.getEpicTasksById(2);
		tm.deleteAllItems();

		System.out.println(tm);
	}
}
