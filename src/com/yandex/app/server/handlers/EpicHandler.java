package com.yandex.app.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskTypes;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

public class EpicHandler extends TaskHandler {

	private enum Endpoint {
		GET_EPICS, GET_EPIC,
		GET_EPIC_SUBTASKS,
		POST_EPIC,
		DELETE_EPIC,
		UNKNOWN
	}

	public EpicHandler(TaskManager taskManager, Gson gson) {
		super(taskManager, gson);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Endpoint endpoint = getEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());
		Optional<Integer> taskId;
		try {
			switch (endpoint) {
				case GET_EPICS -> sendSuccess(exchange, gson.toJson(taskManager.getEpics()));
				case GET_EPIC, GET_EPIC_SUBTASKS -> {
					taskId = getTaskId(exchange.getRequestURI().getPath());
					if (taskId.isEmpty()) {
						sendNotAcceptable(exchange, "Некорректный идентификатор эпика");
						return;
					}
					Optional<Task> taskOpt = Optional.ofNullable(taskManager.getItemById(taskId.get()));
					if (taskOpt.isEmpty() || taskOpt.get().getTaskType() != TaskTypes.EPIC) {
						sendNotFound(exchange, "Не удалось получить эпик с данным идентификатором");
						return;
					}
					if (endpoint == Endpoint.GET_EPIC_SUBTASKS) {
						sendSuccess(exchange, gson.toJson(taskManager.getEpicSubtasksById(taskOpt.get().getId())));
					} else {
						sendSuccess(exchange, gson.toJson(taskOpt.get()));
					}
				}
				case POST_EPIC -> {
					Optional<Task> taskOptional = parseTask(exchange.getRequestBody());
					if (taskOptional.isEmpty()) throw new RuntimeException();

					Task task = taskOptional.get();
					boolean addedOrUpdated = task.getId() > 0 ? taskManager.updateItem(task) : taskManager.addNewItem(task);
					if (addedOrUpdated) {
						sendCreatedSuccess(exchange);
					} else {
						sendNotAcceptable(exchange, "Не удалось добавить эпик");
					}
				}
				case DELETE_EPIC -> {
					taskId = getTaskId(exchange.getRequestURI().getPath());
					if (taskId.isEmpty()) {
						sendNotAcceptable(exchange, "Некорректный идентификатор эпика");
						return;
					}
					taskManager.deleteItemById(taskId.get());
					sendSuccess(exchange, "");
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

		if (pathParts.length == 4 && pathParts[3].equals("subtasks")
				&& "GET".equals(method)) return Endpoint.GET_EPIC_SUBTASKS;

		if (pathParts.length == 3) {
			if ("GET".equals(method)) return Endpoint.GET_EPIC;
			if ("DELETE".equals(method)) return Endpoint.DELETE_EPIC;
		}
		if (pathParts.length == 2) {
			if ("GET".equals(method)) return Endpoint.GET_EPICS;
			if ("POST".equals(method)) return Endpoint.POST_EPIC;
		}
		return Endpoint.UNKNOWN;
	}

	@Override
	public String getMainPathPart() {
		return "epics";
	}

	@Override
	protected Type getTypeForParse() {
		return Epic.class;
	}
}
