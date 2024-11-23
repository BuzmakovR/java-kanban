package com.yandex.app.exception;

public class ManagerSaveException extends RuntimeException {
	static final String defaultPartMessage = "Произошла ошибка при сохранении";

	public ManagerSaveException() {
		super(defaultPartMessage);
	}

	public ManagerSaveException(final String message) {
		super(String.format("%s: %s", defaultPartMessage, message));
	}
}
