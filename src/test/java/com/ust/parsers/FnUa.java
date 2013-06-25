package com.ust.parsers;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

public class FnUa {

	private static Logger log = LogManager.getLogger(FnUa.class);

	private String kievAll;
	private String favoriteFilter;

	private static final int TIMEOUT = 1000 * 30;

	@Before
	public void init() {
		log.trace("Loading properties...");
		Properties props = new Properties();
		String resource = "fn.ua.properties";
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
		log.info("loaded property kiev.all is: " + kievAll);

		favoriteFilter = props.getProperty("favorite.filter");
		log.info("loaded property \"favorite\" is: " + favoriteFilter);
	}

	@Test
	public void grab() {
		try {
			log.trace("Connecting to fn.ua...");
			Connection doc = Jsoup.connect(favoriteFilter).timeout(TIMEOUT);

			scan(doc);
			extract();

		} catch (IOException e) {
			String msg = "connecting to FN.UA failed";
			log.error(msg, e);
			fail(msg);
		}
	}

	private void scan(Connection con) throws IOException {
		Set<String> urls = new HashSet<String>();
		Document doc = con.get();
		// FIXME doing by page counter neither duplicate occurrence 
		outer: for (int i = 1;; con.data("p", "" + ++i)) {
			doc = con.get();
			log.info("page: " + i);
			
			for (Element e : doc.select(".offer-photo a")) {
				String url = e.attr("href");
				if (!urls.add(url)) {
					break outer;
				}
				log.info("find url: " + url);
			}
		}		
	}

	private void extract() {
		// parse separate items

	}
}
