package com.ust.model;

import java.util.HashSet;
import java.util.Set;

public class Phone {
	private String _id;
	private boolean broker;
	private boolean checked;
	private Set<String> related;

	public Phone(String id, HashSet<String> related) {
		this._id = id;
		this.related = related;
	}

	Phone() {
	}

	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public boolean isBroker() {
		return broker;
	}

	public void setBroker(boolean broker) {
		this.broker = broker;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Set<String> getRelated() {
		return related;
	}

	public void setRelated(Set<String> related) {
		this.related = related;
	}
}