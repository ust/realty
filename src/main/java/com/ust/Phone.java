package com.ust;

import java.util.HashSet;
import java.util.Set;

public class Phone {
	String _id;
	boolean broker;
	boolean checked;
	Set<String> related;

	Phone(String id, HashSet<String> related) {
		this._id = id;
		this.related = related;
	}

	Phone() {
	}
}