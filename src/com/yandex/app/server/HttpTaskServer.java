package com.yandex.app.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import com.yandex.app.server.handlers.*;
import com.yandex.app.server.typeAdapters.DurationTypeAdapter;
import com.yandex.app.server.typeAdapters.LocalDateTimeTypeAdapter;
import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

	private static final int PORT = 8080;
	private final HttpServer httpServer;
	private final TaskManager taskManager;
	private final Gson gson;

	public HttpTaskServer(TaskManager taskManager) throws IOException {
		this.taskManager = taskManager;
		httpServer = HttpServer.create();
		httpServer.bind(new InetSocketAddress(PORT), 0);

		gson = new GsonBuilder()
				.serializeNulls()
				.registerTypeAdapter(Duration.class, new DurationTypeAdapter())
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
				.create();

		TaskHandler taskHandler = new TaskHandler(taskManager, gson);
		httpServer.createContext("/" + taskHandler.getMainPathPart(), taskHandler);
		taskHandler = new SubtaskHandler(taskManager, gson);
		httpServer.createContext("/" + taskHandler.getMainPathPart(), taskHandler);
		taskHandler = new EpicHandler(taskManager, gson);
		httpServer.createContext("/" + taskHandler.getMainPathPart(), taskHandler);
		HistoryHandler historyHandler = new HistoryHandler(taskManager, gson);
		httpServer.createContext("/" + historyHandler.getMainPathPart(), historyHandler);
		PrioritizedHandler prioritizedHandler = new PrioritizedHandler(taskManager, gson);
		httpServer.createContext("/" + prioritizedHandler.getMainPathPart(), prioritizedHandler);
	}

	public Gson getGson() {
		return gson;
	}

	public void start() throws IOException {
		httpServer.start();
		System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
	}

	public void stop() {
		httpServer.stop(0);
	}

	public static void main(String[] args) {
		String pathName = "./data/taskManagerData.csv";

		try {
			File file = new File(pathName);
			if (!file.exists()) {
				if (!file.createNewFile()) throw new NoSuchFileException("Не удалось создать новый файл");
			}
			HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getBackedTaskManager(file));
			/*HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());*/
			httpTaskServer.start();
		} catch (NoSuchFileException e) {
			System.out.println("Не удалось получить файл: " + pathName + ". " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
