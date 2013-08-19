package com.ust;

import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class AdvertService {
	private static Logger log = LogManager.getLogger(AdvertService.class);

	private MongoClient mongo;
	private Jongo jongo;
	private DB db;
	private MongoCollection adverts;

	public void startup(String dbName) throws UnknownHostException {
		mongo = new MongoClient();
		db = mongo.getDB(dbName);
		jongo = new Jongo(db);
		adverts = jongo.getCollection("advert");

	}

	public void save(Advert ad) {
		adverts.save(ad);
	}

	public void save(Iterable<Advert> ads) {
		// save each url to separate file
		for (Advert ad : ads) {
			adverts.save(ad);
		}
	}

	public Iterator<Advert> iterator(boolean processed) {
		return adverts.find(processed ? "" : "{processed:'false'}")
				.as(Advert.class).iterator();
	}

	public void shutdown() {
		mongo.close();
		log.debug("mongo client closed");

	}

}
