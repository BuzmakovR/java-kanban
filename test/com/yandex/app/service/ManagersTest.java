package com.yandex.app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

	@Test
	void managerMustReturnTaskManager() {
		assertNotNull(Managers.getDefault(), "Managers не возвращает менеджер задач");
		assertNotNull(Managers.getDefaultHistory(), "Managers не возвращает менеджер истории");
	}
}
