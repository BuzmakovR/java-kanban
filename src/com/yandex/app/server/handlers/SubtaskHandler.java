package com.yandex.app.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskTypes;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

public class SubtaskHandler extends TaskHandler {

	private enum Endpoint {
		GET_SUBTASKS,
		GET_SUBTASK,
		POST_SUBTASK,
		DELETE_SUBTASK,
		UNKNOWN
	}

	public SubtaskHandler(TaskManager taskManager, Gson gson) {
		super(taskManager, gson);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Endpoint endpoint = getEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());
		Optional<Integer> taskId;
		try {
			switch (endpoint) {
				case GET_SUBTASKS -> sendSuccess(exchange, gson.toJson(taskManager.getSubtasks()));
				case GET_SUBTASK, DELETE_SUBTASK -> {
					taskId = getTaskId(exchange.getRequestURI().getPath());
					if (taskId.isEmpty()) {
						sendNotAcceptable(exchange, "Некорректный идентификатор подзадачи");
						return;
					}
					if (endpoint == Endpoint.GET_SUBTASK) {
						Optional<Task> taskOpt = Optional.ofNullable(taskManager.getItemById(taskId.get()));
						if (taskOpt.isEmpty() || taskOpt.get().getTaskType() != TaskTypes.SUBTASK) {
							sendNotFound(exchange, "Не удалось получить подзадачу с данным идентификатором");
						} else {
							sendSuccess(exchange, gson.toJson(taskOpt.get()));
						}
					} else {
						taskManager.deleteItemById(taskId.get());
						sendSuccess(exchange, "");
					}
				}
				case POST_SUBTASK -> {
					Optional<Task> taskOptional = parseTask(exchange.getRequestBody());
					if (taskOptional.isEmpty()) throw new RuntimeException();

					Task task = taskOptional.get();
					boolean addedOrUpdated = task.getId() > 0 ? taskManager.updateItem(task) : taskManager.addNewItem(task);
					if (addedOrUpdated) {
						sendCreatedSuccess(exchange);
					} else {
						sendNotAcceptable(exchange, "Не удалось добавить подзадачу");
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
			if ("GET".equals(method)) return Endpoint.GET_SUBTASK;
			if ("DELETE".equals(method)) return Endpoint.DELETE_SUBTASK;
		}
		if (pathParts.length == 2) {
			if ("GET".equals(method)) return Endpoint.GET_SUBTASKS;
			if ("POST".equals(method)) return Endpoint.POST_SUBTASK;
		}
		return Endpoint.UNKNOWN;
	}

	@Override
	public String getMainPathPart() {
		return "subtasks";
	}

	@Override
	protected Type getTypeForParse() {
		return Subtask.class;
	}
}
