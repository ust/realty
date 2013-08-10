package com.ust.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class FnUa {

	private static Logger log = LogManager.getLogger(FnUa.class);

	private int timeout = 1000 * 30;

	private String kievAll;
	private String favoriteFilter;
	private String dbName;
	private int topLim;

	private Connection con;
	private HashSet<Advert> ads;

	@Before
	public void loadProperties() {
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

		dbName = props.getProperty("db.name");
		log.info("loaded property \"db.name\" is: " + dbName);

		timeout = Integer.parseInt(props.getProperty("timeout"));
		log.info("loaded property \"timeout\" is: " + timeout);
		
		topLim = Integer.parseInt(props.getProperty("pages.depth"));
		log.info("loaded property \"pages.depth\" is: " + topLim);
	}

	@Test
	public void grab() {
		connect();

		try {
			log.info("Scanning links in filter...");
			scan();
		} catch (IOException e) {
			String msg = "connecting to FN.UA failed";
			log.error(msg, e);
			fail(msg);
		}

		try {
			log.info("Saving all collected links...");
			save();
		} catch (UnknownHostException e) {
			log.error(e);
			fail("DB problems");
		}

		extract();
		
		log.info("Fn.ua parsed successfully");
	}

	/**
	 * 
	 */
	private void connect() {
		log.trace("Connecting to fn.ua...");
		con = Jsoup.connect(favoriteFilter).timeout(timeout);
	}

	private void scan() throws IOException {
		ads = new HashSet<Advert>();
		Document doc = con.get();
		// FIXME doing by page counter neither duplicate occurrence
		outer: for (int i = 1; i <= topLim; con.data("p", "" + ++i)) {
			doc = con.get();
			log.debug("page: " + i);
	
			for (Element e : doc.select(".offer-photo a")) {
				String url = e.attr("href");
				if (!ads.add(new Advert(url))) {
					break outer;
				}
				log.trace("find url: " + url);
			}
		}
	}

	private void extract() {
		// TODO parse separate items
		// save to appropriate file
	}

	private void save() throws UnknownHostException {
		DB db = new MongoClient().getDB(dbName);
		Jongo jongo = new Jongo(db);
		MongoCollection adverts = jongo.getCollection("advert");
		// save each url to separate file
		for (Advert ad : ads) {
			adverts.save(ad);
		}		
	}

	// @Test
	public void parseUrl() {
		String url = "view.php?ad_id=3611919&page=0&adtype=2";
		String regex = "(?<=ad_id=)\\d+";
	
		Matcher m = Pattern.compile(regex).matcher(url);
		m.find();
		String replacement = m.group();
	
		log.debug("url: \"" + url + "\"" + " repl: \"" + replacement + "\"");
	
		assertEquals("url didn't parsed properly", "3611919", replacement);
	}
}

class Advert {
	static final String REGEX_ID = "(?<=ad_id=)\\d+";

	public Advert(String url) {
		Matcher m = Pattern.compile(REGEX_ID).matcher(url);
		m.find();
		this._id = Long.parseLong(m.group());
		this.url = url;
	}

	long _id;
	String url;
}
