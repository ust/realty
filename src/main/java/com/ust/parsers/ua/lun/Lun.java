package com.ust.parsers.ua.lun;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Lun {
	private static Logger log = LogManager.getLogger(Lun.class);

	public boolean check(String phone) {
		boolean broker = false;
		try {
			Document doc = Jsoup.connect(
					"http://www.lun.ua/" + phone + "-телефон").get();
			String text = doc.select("h1").text();
			log.trace("h1 text: " + text);
			broker = text.contains("телефон риелтора");
		} catch (IOException e) {
			log.error("atempt to connect with LUN.UA failed", e);
		}
		if (!broker) {
			log.info("ad without broker found!");
		}
		return broker;
	}

}
