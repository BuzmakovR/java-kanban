package com.yandex.app.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatuses;
import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

	TaskManager taskManager = Managers.getDefault();
	HttpTaskServer taskServer = new HttpTaskServer(taskManager);
	Gson gson = taskServer.getGson();

	public HttpTaskServerTest() throws IOException {
	}

	@BeforeEach
	public void setUp() {
		taskManager.deleteAllItems();
		try {
			taskServer.start();
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@AfterEach
	public void shutDown() {
		taskServer.stop();
	}

	@Test
	public void getNotFound() {
		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/tasks_get");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(404, response.statusCode(), "Некорректный статус ответа");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	//region TASK
	@Test
	public void getTasks() {
		Task task = new Task("Task", "Task", TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60));
		assertTrue(taskManager.addNewItem(task), "Не удалось добавить задачу");

		try (HttpClient client = HttpClient.newHttpClient()) {

			// Получение списка задач
			URI url = URI.create("http://localhost:8080/tasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список задач");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список задач");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Task.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить задачу из json");
			assertEquals(task, taskOpt.get(), "Получена некорректная задача");
			assertEquals(task.toString(), taskOpt.get().toString(), "Получена некорректная задача");

			// Получение задачи по ID
			url = URI.create("http://localhost:8080/tasks/" + task.getId());
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список задач");

			taskOpt = Optional.ofNullable(gson.fromJson(response.body(), Task.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить задачу из json");
			assertEquals(task, taskOpt.get(), "Получена некорректная задача");
			assertEquals(task.toString(), taskOpt.get().toString(), "Получена некорректная задача");

			// Получение несуществующей задачи
			url = URI.create("http://localhost:8080/tasks/" + 999);
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(404, response.statusCode(), "Некорректный статус ответа");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void addTask() {
		Task task = new Task("Task", "Task", TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60));
		String taskJson = gson.toJson(task);

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/tasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось добавить задачу");

			List<Task> tasksFromManager = taskManager.getTasks();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
			assertEquals("Task", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");

			task = new Task("Task2", "Task2", TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60));
			taskJson = gson.toJson(task);

			request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(406, response.statusCode(), "Получен некорректный статус");

		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void updateTask() {
		Task task = new Task("Task", "Task", TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60));
		assertTrue(taskManager.addNewItem(task), "Не удалось добавить задачу");

		Task taskUpdate = new Task("TaskUpdate", "TaskUpdate", task.getStatus(), task.getStartTime(), task.getDuration());
		taskUpdate.setId(task.getId());

		String taskJson = gson.toJson(taskUpdate);
		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/tasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось обновить задачу");

			List<Task> tasksFromManager = taskManager.getTasks();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
			assertEquals("TaskUpdate", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
			assertEquals(taskUpdate.toString(), tasksFromManager.getFirst().toString(), "Получена некорректная задача");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void deleteTask() {
		Task task = new Task("Task", "Task", TaskStatuses.NEW, LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60));
		assertTrue(taskManager.addNewItem(task), "Не удалось добавить задачу");

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.DELETE()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось удалить задачу");

			List<Task> tasksFromManager = taskManager.getTasks();
			assertEquals(0, tasksFromManager.size(), "Задача не удалена");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}
	//endregion

	//region EPIC
	@Test
	public void getEpics() {
		Task task = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(task), "Не удалось добавить эпик");

		try (HttpClient client = HttpClient.newHttpClient()) {
			// Получение списка задач
			URI url = URI.create("http://localhost:8080/epics");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список эпиков");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список эпиков");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Epic.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить эпик из json");
			assertEquals(task, taskOpt.get(), "Получена некорректный эпик");
			assertEquals(task.toString(), taskOpt.get().toString(), "Получена некорректный эпик");

			// Получение задачи по ID
			url = URI.create("http://localhost:8080/epics/" + task.getId());
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список эпиков");

			taskOpt = Optional.ofNullable(gson.fromJson(response.body(), Epic.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить эпик из json");
			assertEquals(task, taskOpt.get(), "Получена некорректный эпик");
			assertEquals(task.toString(), taskOpt.get().toString(), "Получена некорректный эпик");

			// Получение несуществующей задачи
			url = URI.create("http://localhost:8080/epics/" + 999);
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(404, response.statusCode(), "Некорректный статус ответа");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void getEpicSubtasks() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Task subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtask), "Не удалось добавить подзадачу");

		try (HttpClient client = HttpClient.newHttpClient()) {
			// Получение списка задач
			URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список подзадач");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список подзадач");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtask, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtask.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void addEpic() {
		Task epic = new Epic("Epic", "Epic");
		String taskJson = gson.toJson(epic);

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/epics");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось добавить эпик");

			List<Epic> tasksFromManager = taskManager.getEpics();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
			assertEquals(epic.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя эпика");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void updateEpic() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");

		Task epicUpdate = new Epic("EpicUpdate", "EpicUpdate");
		epicUpdate.setId(epic.getId());

		String taskJson = gson.toJson(epicUpdate);
		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/epics");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось обновить эпик");

			List<Epic> tasksFromManager = taskManager.getEpics();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
			assertEquals(epicUpdate.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя эпика");
			assertEquals(epicUpdate.toString(), tasksFromManager.getFirst().toString(), "Получена некорректный эпик");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void deleteEpic() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.DELETE()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось удалить эпик");

			List<Epic> tasksFromManager = taskManager.getEpics();
			assertEquals(0, tasksFromManager.size(), "Эпик не удалена");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}
	//endregion

	//region SUBTASKS
	@Test
	public void getSubtasks() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Task subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtask), "Не удалось добавить подзадачу");

		try (HttpClient client = HttpClient.newHttpClient()) {
			// Получение списка задач
			URI url = URI.create("http://localhost:8080/subtasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список подзадач");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список подзадач");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtask, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtask.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");

			// Получение задачи по ID
			url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список подзадач");

			taskOpt = Optional.ofNullable(gson.fromJson(response.body(), Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtask, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtask.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");

			// Получение несуществующей задачи
			url = URI.create("http://localhost:8080/subtasks/" + 999);
			request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(404, response.statusCode(), "Некорректный статус ответа");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void addSubtask() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Task subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		String taskJson = gson.toJson(subtask);

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/subtasks");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось добавить подзадачу");

			List<Subtask> tasksFromManager = taskManager.getSubtasks();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
			assertEquals(subtask.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");

			Task subtask2 = new Subtask("Subtask2", "Subtask2", TaskStatuses.NEW,
					LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
			taskJson = gson.toJson(subtask);

			request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(406, response.statusCode(), "Получен некорректный статус");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void updateSubtask() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Subtask subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtask), "Не удалось добавить подзадачу");
		Task subtaskUpdate = new Subtask("SubtaskUpdate", "SubtaskUpdate", subtask.getStatus(), subtask.getStartTime(), subtask.getDuration(), subtask.getEpicId());
		subtaskUpdate.setId(subtask.getId());
		String taskJson = gson.toJson(subtaskUpdate);

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/subtasks/");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.POST(HttpRequest.BodyPublishers.ofString(taskJson))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, response.statusCode(), "Не удалось добавить подзадачу");

			List<Subtask> tasksFromManager = taskManager.getSubtasks();
			assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
			assertEquals(subtaskUpdate.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
			assertEquals(subtaskUpdate.toString(), tasksFromManager.getFirst().toString(), "Получена некорректная подзадача");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void deleteSubtask() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Subtask subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtask), "Не удалось добавить подзадачу");

		try (HttpClient client = HttpClient.newHttpClient()) {
			URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.DELETE()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось удалить подзадачу");

			List<Subtask> tasksFromManager = taskManager.getSubtasks();
			assertEquals(0, tasksFromManager.size(), "Подзадача не удалена");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}
	//endregion

	//region HISTORY
	@Test
	public void getHistory() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		Task subtask = new Subtask("Subtask", "Subtask", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 1), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtask), "Не удалось добавить подзадачу");
		Task task = new Task("Task", "Task", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 2, 1), Duration.ofMinutes(60));
		assertTrue(taskManager.addNewItem(task), "Не удалось добавить задачу");

		taskManager.getItemById(task.getId());
		taskManager.getItemById(subtask.getId());
		taskManager.getItemById(epic.getId());

		try (HttpClient client = HttpClient.newHttpClient()) {
			// Получение списка задач
			URI url = URI.create("http://localhost:8080/history");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить историю задач");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список истории задач");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			assertEquals(3, jsonArr.size(), "Получена некорректная длина списка истории задач");

			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Task.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить задачу из json");
			assertEquals(task, taskOpt.get(), "Получена некорректная задача");
			assertEquals(task.toString(), taskOpt.get().toString(), "Получена некорректная задача");

			jsonEl = jsonArr.get(1);
			taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtask, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtask.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");

			jsonEl = jsonArr.get(2);
			taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Epic.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить эпик из json");
			assertEquals(epic, taskOpt.get(), "Получен некорректный эпик");
			assertEquals(epic.toString(), taskOpt.get().toString(), "Получен некорректный эпик");

		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}
	//endregion

	//region PRIORITIZED
	@Test
	public void getPrioritized() {
		Task epic = new Epic("Epic", "Epic");
		assertTrue(taskManager.addNewItem(epic), "Не удалось добавить эпик");
		// Time 03:00 - 04:00
		Task subtaskPriority3 = new Subtask("Subtask", "Priority3", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 3, 0), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtaskPriority3), "Не удалось добавить подзадачу");
		// Time 01:00 - 02:00
		Task subtaskPriority1 = new Subtask("Subtask2", "Priority1", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 1, 0), Duration.ofMinutes(60), epic.getId());
		assertTrue(taskManager.addNewItem(subtaskPriority1), "Не удалось добавить подзадачу");
		// Time 02:00 - 03:00
		Task taskPriority2 = new Task("Task", "Priority2", TaskStatuses.NEW,
				LocalDateTime.of(2020, 1, 1, 2, 0), Duration.ofMinutes(60));
		assertTrue(taskManager.addNewItem(taskPriority2), "Не удалось добавить задачу");

		try (HttpClient client = HttpClient.newHttpClient()) {
			// Получение списка задач
			URI url = URI.create("http://localhost:8080/prioritized");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Не удалось получить список приоритетных задач");

			JsonElement jsonElement = JsonParser.parseString(response.body());
			assertTrue(jsonElement.isJsonArray(), "Не возвращается список приоритетных задач");
			JsonArray jsonArr = jsonElement.getAsJsonArray();
			assertEquals(3, jsonArr.size(), "Получена некорректная длина список приоритетных задач");

			JsonElement jsonEl = jsonArr.get(0);
			Optional<Task> taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtaskPriority1, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtaskPriority1.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");

			jsonEl = jsonArr.get(1);
			taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Task.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить задачу из json");
			assertEquals(taskPriority2, taskOpt.get(), "Получена некорректная задача");
			assertEquals(taskPriority2.toString(), taskOpt.get().toString(), "Получена некорректная задача");

			jsonEl = jsonArr.get(2);
			taskOpt = Optional.ofNullable(gson.fromJson(jsonEl, Subtask.class));
			assertTrue(taskOpt.isPresent(), "Не удалось получить подзадачу из json");
			assertEquals(subtaskPriority3, taskOpt.get(), "Получена некорректная подзадача");
			assertEquals(subtaskPriority3.toString(), taskOpt.get().toString(), "Получена некорректная подзадача");
		} catch (IOException | InterruptedException e) {
			Assertions.fail(e.getMessage());
		}
	}
	//endregion
}
