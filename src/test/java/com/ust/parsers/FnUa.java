package com.ust.parsers;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FnUa {
	private static Logger log = LogManager.getLogger(FnUa.class);

	@SuppressWarnings("unused")
	private String kievAll = "http://fn.ua/listing.php?parent_id%5B%5D=1&parent_id%5B%5D=9&parent_id%5B%5D=51&parent_id%5B%5D=all";
	private String favoriteFilter = "http://fn.ua/listing.php?parent_id%5B0%5D=1&parent_id%5B1%5D=9&parent_id%5B2%5D=51&parent_id%5B3%5D=all&pricemin=2500&pricemax=3500&pricecur=1&rooms=1";

	@Before
	public void init() {
		log.trace("Loading properties...");
		Properties props = new Properties();
		String resource = "fn.ua";
		try {
			InputStream in = FnUa.class.getResourceAsStream(resource);
			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (IOException e) {
			log.error("properties \"" + resource + "\" couldn't be load", e);
		}

		kievAll = props.getProperty("kiev.all");
		favoriteFilter = props.getProperty("favorite");
	}

	@Test
	public void grab() {
		try {
			Document doc = Jsoup.connect(favoriteFilter).get();
			log.info(doc.title());

		} catch (IOException e) {
			String msg = "connecting to FN.UA failed";
			log.error(msg, e);
			fail(msg);
		}
	}
}
