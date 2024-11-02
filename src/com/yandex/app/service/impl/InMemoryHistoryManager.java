package com.yandex.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
		removeNode(nodes.get(task.getId()));
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
		removeNode(node);
	}

	@Override
	public void removeAll() {
		nodes.clear();
		history.removeAll();
	}

	private void removeNode(Node<Task> node) {
		if (node == null) return;
		history.removeItem(node);
	}

	private static class PrimitiveLinkedList<T> {
		private Node<T> first;
		private Node<T> last;

		Node<T> linkLast(T item) {
			Node<T> node = new Node<>(item, last, null);
			if (first == null) {
				first = node;
			} else {
				last.setNext(node);
			}
			last = node;
			return node;
		}

		void removeItem(Node<T> item) {
			final Node<T> itemNext = item.getNext();
			final Node<T> itemPrev = item.getPrev();

			if (item.equals(first)) first = itemNext;
			if (item.equals(last)) last = itemPrev;

			if (itemNext != null) itemNext.setPrev(itemPrev);
			if (itemPrev != null) itemPrev.setNext(itemNext);
		}

		void removeAll() {
			first = last = null;
		}

		List<T> getItems() {
			List<T> tasks = new LinkedList<>();
			Node<T> nextNode = first;
			while (nextNode != null) {
				tasks.add(nextNode.getData());
				nextNode = nextNode.getNext();
			}
			return tasks;
		}
	}
}
