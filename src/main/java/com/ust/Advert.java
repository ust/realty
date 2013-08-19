package com.ust;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Advert {
	public static final String REGEX_ID = "(?<=ad_id=)\\d+";

	public Advert() {
		// convenience for unmarshalling
	}

	public Advert(String url) {
		Matcher m = Pattern.compile(REGEX_ID).matcher(url);
		m.find();
		this._id = Long.parseLong(m.group());
		this.url = url;
		this.processed = false;
	}

	private long _id;
	private String url;
	private String title;
	private String description;
	private String price;
	private List<String> imgs;
	private List<String> phones;
	private String date;
	private boolean processed;

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public List<String> getImgs() {
		return imgs;
	}

	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
}
