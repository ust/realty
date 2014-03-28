package com.ust.realty.model;

import java.util.HashSet;
import java.util.Set;

public class Phone {
	private String _id;
	private boolean broker;
	private boolean checked;
	private Set<String> related;
	private Set<String> ads;

	public Phone(String id, String ad, HashSet<String> related) {
		this._id = id;
		this.related = related;

		if (ad != null) {
			this.ads = new HashSet<String>();
			this.ads.add(ad);
		}
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

	public Set<String> getAds() {
		return ads;
	}

	public void setAds(Set<String> ads) {
		this.ads = ads;
	}
}