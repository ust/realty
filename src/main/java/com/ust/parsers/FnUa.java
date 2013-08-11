package com.ust.parsers;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class FnUa {

	private static Logger log = LogManager.getLogger(FnUa.class);

	// properties
	private String url;
	private String kievAll;
	private String favoriteFilter;
	private int timeout;
	private String dbName;
	private String imgDir;
	private int topLim;
	private boolean forceUpdate;
	// cashe
	private MongoClient mongoClient;
	private Jongo jongo;
	private DB db;
	private MongoCollection adverts;
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

		url = props.getProperty("url");
		log.debug("loaded property kiev.all is: " + url);

		kievAll = props.getProperty("kiev.all");
		log.debug("loaded property kiev.all is: " + kievAll);

		favoriteFilter = props.getProperty("favorite.filter");
		log.debug("loaded property \"favorite\" is: " + favoriteFilter);

		dbName = props.getProperty("db.name");
		log.debug("loaded property \"db.name\" is: " + dbName);

		imgDir = props.getProperty("img.dir");
		log.debug("loaded property \"img.dir\" is: " + imgDir);

		timeout = Integer.parseInt(props.getProperty("timeout"));
		log.debug("loaded property \"timeout\" is: " + timeout);

		topLim = Integer.parseInt(props.getProperty("pages.depth"));
		log.debug("loaded property \"pages.depth\" is: " + topLim);

		forceUpdate = Boolean.parseBoolean(props.getProperty("foce.update"));
		log.debug("loaded property \"foce.update\" is: " + forceUpdate);
	}

	@Test
	public void grab() {
		long start = System.currentTimeMillis();

		try {
			connectToSite();

			log.info("Scanning links in filter...");
			scan();

			log.info("Saving all collected links...");
			connectToDB();
			saveScanned();

			extract();
			download();
		} catch (UnknownHostException e) {
			log.error(e);
			fail("DB problems");
		} catch (IOException e) {
			String msg = "connecting to FN.UA failed";
			log.error(msg, e);
			fail(msg);
		} catch (Exception e) {
			log.error(e);
			fail("See logs for details");
		}

		log.info("Fn.ua parsed successfully in "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");
	}

	/**
	 * @throws UnknownHostException
	 * 
	 */
	private void connectToSite() {
		log.trace("Connecting to fn.ua...");
		con = Jsoup.connect(url + favoriteFilter).timeout(timeout);
	}

	private void scan() throws IOException {
		ads = new HashSet<Advert>();
		Document doc = con.get();
		// XXX doing by page counter neither duplicate occurrence
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

	private void connectToDB() throws UnknownHostException {
		mongoClient = new MongoClient();
		db = mongoClient.getDB(dbName);
		jongo = new Jongo(db);
		adverts = jongo.getCollection("advert");

	}

	private void saveScanned() {
		// save each url to separate file
		for (Advert ad : ads) {
			adverts.save(ad);
		}
	}

	private void extract() {
		// load unprocessed items
		for (Iterator<Advert> i = adverts
				.find(forceUpdate ? "" : "{processed:'false'}")
				.as(Advert.class).iterator(); i.hasNext();) {
			Advert ad = i.next();

			// save to appropriate file
			if (parse(ad)) {
				ad.processed = true;
				adverts.save(ad);
			}
		}
	}

	private boolean parse(Advert ad) {
		// parse separate items
		Document doc = null;
		try {
			doc = con.url(url + ad.url).get();
		} catch (IOException e) {
			log.error("failed connection to " + url + ad.url);
			return false;
		}
		ad.title = doc.select("h1").text();
		ad.description = doc.select("p.ad-desc").text();
		ad.price = doc.select("p.ad-price b").text();
		ad.date = doc.select("p.ad-pub-date b").first().text();
		// collect images urls
		Elements imgs = doc.select("#ad-thumbs a.highslide");
		if (!imgs.isEmpty()) {
			ad.imgs = new ArrayList<String>();
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				ad.imgs.add(i.next().attr("href"));
			}
		}

		log.debug("parsed id " + ad._id + " price " + ad.price
				+ " images count " + ad.imgs.size());
		return true;
	}

	private void download() throws IOException {
		String currDir = new File("").getCanonicalPath();
		File imgs = new File(currDir + imgDir);
		if (!imgs.exists()) {
			imgs.mkdir();
		}
		// download images...
	}

}

class Advert {
	static final String REGEX_ID = "(?<=ad_id=)\\d+";

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

	long _id;
	String url;
	public String title;
	public String description;
	public String price;
	public List<String> imgs;
	public String date;
	boolean processed;
}
