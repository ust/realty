package com.ust;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.MongoClient;

public class AdvertService {
	private static Logger log = LogManager.getLogger(AdvertService.class);

	private MongoClient mongo;
	private Jongo jongo;
	private MongoCollection adverts;
	private MongoCollection phones;

	public void startup(String dbName) throws UnknownHostException {
		mongo = new MongoClient();
		jongo = new Jongo(mongo.getDB(dbName));
		adverts = jongo.getCollection("adverts");
		phones = jongo.getCollection("phones");

		log.debug("mongo client started up");

	}

	public void save(Advert ad) {
		adverts.save(ad);
		log.debug("ad \\w id:" + ad.get_id() + " saved");

		List<String> adPhones = ad.getPhones();
		if (adPhones != null && !adPhones.isEmpty()) {
			// get existed phones
			String query = "{field:{$in:" + adPhones.toArray(new String[0])
					+ "}}";
			log.trace("quering as " + query);
			for (Iterator<Phone> i = phones.find(query).as(Phone.class)
					.iterator(); i.hasNext();) {
				for (String adPhone : adPhones) {
					// add number
					// add related numbers
					break;
				}
			}
		} else {
			log.debug("ad has no phones");
		}
	}

	public void save(Iterable<Advert> ads) {
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

class Phone {
	String _id;
	List<String> related;
}
