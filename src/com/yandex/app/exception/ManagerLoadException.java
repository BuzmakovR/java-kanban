package com.yandex.app.exception;

public class ManagerLoadException extends RuntimeException {
	static final String defaultPartMessage = "Произошла ошибка при загрузке данных";

	public ManagerLoadException() {
		super(defaultPartMessage);
	}

	public ManagerLoadException(final String message) {
		super(String.format("%s: %s", defaultPartMessage, message));
	}
}