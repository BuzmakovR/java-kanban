package com.yandex.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.yandex.app.model.Task;
import com.yandex.app.service.HistoryManager;
import com.yandex.app.util.Node;

public class InMemoryHistoryManager implements HistoryManager {
	private final PrimitiveLinkedList<Task> history;
	private final HashMap<Integer, Node<Task>> nodes;

	public InMemoryHistoryManager() {
		this.history = new PrimitiveLinkedList<>();
		this.nodes = new HashMap<>();
	}

	@Override
	public void add(Task task) {
		if (nodes.containsKey(task.getId())) {
			removeNode(nodes.get(task.getId()));
		}
		Node<Task> node = history.linkLast(task);
		nodes.put(task.getId(), node);
	}

	@Override
	public List<Task> getHistory() {
		return history.getItems();
	}

	@Override
	public void remove(int id) {
		Node<Task> node = nodes.remove(id);
		if (node != null) {
			removeNode(node);
		}
	}

	@Override
	public void removeAll() {
		nodes.clear();
		history.removeAll();
	}

	private void removeNode(Node<Task> node) {
		history.removeItem(node);
	}

	private static class PrimitiveLinkedList<T> {
		private Node<T> first;
		private Node<T> last;

		PrimitiveLinkedList() {
		}

		Node<T> linkLast(T item) {
			Node<T> node = new Node<>(item);
			if (first == null) {
				first = last = node;
			} else {
				node.prev = last;
				last.next = node;
				last = node;
			}
			return node;
		}

		void removeItem(Node<T> item) {
			if (item.equals(first)) first = item.next != null ? item.next : null;
			if (item.equals(last)) last = item.prev != null ? item.prev : null;

			if (item.next != null) item.next.prev = item.prev != null ? item.prev : null;
			if (item.prev != null) item.prev.next = item.next != null ? item.next : null;
		}

		void removeAll() {
			first = last = null;
		}

		List<T> getItems() {
			ArrayList<T> tasks = new ArrayList<>();
			Node<T> nextNode = first;
			while (nextNode != null) {
				tasks.add(nextNode.data);
				nextNode = nextNode.next;
			}
			return tasks;
		}
	}
}
