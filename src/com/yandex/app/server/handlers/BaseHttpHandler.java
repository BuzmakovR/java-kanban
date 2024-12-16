package com.yandex.app.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

	protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
		writeResponse(exchange, 200, text);
	}

	protected void sendCreatedSuccess(HttpExchange exchange) throws IOException {
		writeResponse(exchange, 201, "");
	}

	protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
		writeResponse(exchange, 404, text);
	}

	protected void sendNotAcceptable(HttpExchange exchange, String text) throws IOException {
		writeResponse(exchange, 406, text);
	}

	protected void sendServerError(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(500, 0);
		exchange.close();
	}

	private void writeResponse(HttpExchange exchange,
							   int responseCode,
							   String responseString) throws IOException {
		byte[] resp = responseString.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
		exchange.sendResponseHeaders(responseCode, resp.length);
		if (resp.length > 0) {
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(responseString.getBytes(DEFAULT_CHARSET));
			}
		}
		exchange.close();
	}

	public String getMainPathPart() {
		return "base";
	}
}
