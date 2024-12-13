package com.yandex.app.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler {

	private enum Endpoint {GET_TASKS, GET_TASK, POST_TASK, DELETE_TASK, UNKNOWN}

	protected TaskManager taskManager;
	protected Gson gson;

	public TaskHandler(TaskManager taskManager, Gson gson) {
		this.taskManager = taskManager;
		this.gson = gson;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Endpoint endpoint = getEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());
		Optional<Integer> taskId;
		try {
			switch (endpoint) {
				case GET_TASKS -> sendSuccess(exchange, gson.toJson(taskManager.getTasks()));
				case GET_TASK, DELETE_TASK -> {
					taskId = getTaskId(exchange.getRequestURI().getPath());
					if (taskId.isEmpty()) {
						sendNotAcceptable(exchange, "Некорректный идентификатор задачи");
						return;
					}
					if (endpoint == Endpoint.GET_TASK) {
						Optional<Task> taskOpt = Optional.ofNullable(taskManager.getItemById(taskId.get()));
						if (taskOpt.isEmpty()) {
							sendNotFound(exchange, "Не удалось получить задачу с данным идентификатором");
						} else {
							sendSuccess(exchange, gson.toJson(taskOpt.get()));
						}
					} else {
						taskManager.deleteItemById(taskId.get());
						sendSuccess(exchange, "");
					}
				}
				case POST_TASK -> {
					Optional<Task> taskOptional = parseTask(exchange.getRequestBody());
					if (taskOptional.isEmpty()) throw new RuntimeException();

					Task task = taskOptional.get();
					boolean addedOrUpdated = task.getId() > 0 ? taskManager.updateItem(task) : taskManager.addNewItem(task);
					if (addedOrUpdated) {
						sendCreatedSuccess(exchange);
					} else {
						sendNotAcceptable(exchange, "Не удалось добавить задачу");
					}
				}
				default -> sendNotFound(exchange, "");
			}
		} catch (RuntimeException | IOException e) {
			sendServerError(exchange);
		}
	}

	private Endpoint getEndpoint(String method, String path) {
		String[] pathParts = path.split("/");

		if (!getMainPathPart().equals(pathParts[1])) return Endpoint.UNKNOWN;

		if (pathParts.length == 3) {
			if ("GET".equals(method)) return Endpoint.GET_TASK;
			if ("DELETE".equals(method)) return Endpoint.DELETE_TASK;
		}
		if (pathParts.length == 2) {
			if ("GET".equals(method)) return Endpoint.GET_TASKS;
			if ("POST".equals(method)) return Endpoint.POST_TASK;
		}
		return Endpoint.UNKNOWN;
	}

	protected Optional<Integer> getTaskId(String path) {
		try {
			return Optional.of(Integer.parseInt(path.split("/")[2]));
		} catch (NumberFormatException exception) {
			return Optional.empty();
		}
	}

	protected Optional<Task> parseTask(InputStream bodyInputStream) throws IOException {
		String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET).trim();
		return body.isEmpty() ? Optional.empty() : Optional.ofNullable(gson.fromJson(body, getTypeForParse()));
	}

	@Override
	public String getMainPathPart() {
		return "tasks";
	}

	protected Type getTypeForParse() {
		return Task.class;
	}
}
