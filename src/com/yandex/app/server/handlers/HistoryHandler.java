package com.yandex.app.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.Objects;

public class HistoryHandler extends BaseHttpHandler {

	private enum Endpoint {GET_HISTORY, UNKNOWN}

	protected TaskManager taskManager;
	protected Gson gson;

	public HistoryHandler(TaskManager taskManager, Gson gson) {
		this.taskManager = taskManager;
		this.gson = gson;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Endpoint endpoint = getEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());
		try {
			if (Objects.requireNonNull(endpoint) == Endpoint.GET_HISTORY) {
				sendSuccess(exchange, gson.toJson(taskManager.getHistory()));
			} else {
				sendNotFound(exchange, "");
			}
		} catch (RuntimeException | IOException e) {
			sendServerError(exchange);
		}
	}

	private Endpoint getEndpoint(String method, String path) {
		String[] pathParts = path.split("/");
		if ("GET".equals(method)
				&& pathParts.length == 2
				&& getMainPathPart().equals(pathParts[1])) return Endpoint.GET_HISTORY;

		return Endpoint.UNKNOWN;
	}

	@Override
	public String getMainPathPart() {
		return "history";
	}
}
