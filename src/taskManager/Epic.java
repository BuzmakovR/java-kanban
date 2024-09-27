package taskManager;

import java.util.ArrayList;

public class Epic extends Task {
	private final ArrayList<Integer> subtaskIds;

	public Epic(String name, String description) {
		super(name, description, TaskStatuses.NEW, TaskTypes.EPIC);
		subtaskIds = new ArrayList<>();
	}
	public Epic(String name, String description, TaskStatuses status) {
		super(name, description, status, TaskTypes.EPIC);
		subtaskIds = new ArrayList<>();
	}
	public Epic(String name, String description, TaskStatuses status, ArrayList<Integer> subtaskIds) {
		super(name, description, status, TaskTypes.EPIC);
		this.subtaskIds = subtaskIds;
	}

	public ArrayList<Integer> getSubtaskIds() {
		return subtaskIds;
	}
	void addSubtaskIds(Integer subtaskId) {
		if (!this.subtaskIds.contains(subtaskId)) this.subtaskIds.add(subtaskId);
	}
	void deleteSubtaskId(Integer subtaskId) {
		this.subtaskIds.remove(subtaskId);
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", status=" + status +
				", subtaskIds=" + subtaskIds.toString() +
				'}';
	}
}
