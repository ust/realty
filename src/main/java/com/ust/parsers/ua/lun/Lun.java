package com.ust.parsers.ua.lun;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ust.model.Phone;

public class Lun {
	private static Logger log = LogManager.getLogger(Lun.class);

	public void check(Phone phone) throws IOException {
		// connect & handle status
		Response res = Jsoup.connect(
				"http://www.lun.ua/" + phone.getId() + "-телефон").execute();
		int status = res.statusCode();
		switch (status) {
		case 200:
			break;

		case 404:
			log.info("ad with unknown number found!");
			phone.setChecked(true);
			return;
		default:
			log.warn("unhandled http staus " + status);
			phone.setChecked(false);
			return;
		}

		// parse result
		Document doc = res.parse();
		String text = doc.select("h1").text();
		log.trace("h1 text: " + text);

		phone.setBroker(text.contains("телефон риелтора")
				|| text.contains("телефон агентства недвижимости")
				|| text.contains("телефон информационного агентства"));
		phone.setChecked(true);

		if (!phone.isBroker()) {
			if (text.contains("телефон владельца")) {
				log.info(phone.getId() + " owner (!) ");
			}
			if (text.contains("новый телефон")) {
				log.info(phone.getId() + " new (?)");
			} else {
				log.warn(phone.getId() + " unknown (?) text: " + text);
			}
		} else {
			log.trace(phone.getId() + " broker");
		}
	}

}
