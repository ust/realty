package com.ust;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.MongoClient;
import com.ust.model.Advert;
import com.ust.model.Phone;

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

		Set<String> adPhones = ad.getPhones();
		if (adPhones != null && !adPhones.isEmpty()) {
			// create query of existed phones
			String query = "{field:{$in:" + adPhones + "}}";
			log.trace("quering as " + query);

			for (String adPhone : adPhones) {
				boolean found = false;
				Set<String> withouMe = new HashSet<String>(adPhones);
				withouMe.remove(adPhone);

				// update records in db
				for (Iterator<Phone> i = phones.find(query).as(Phone.class)
						.iterator(); i.hasNext();) {
					Phone record = i.next();
					if (adPhone.equals(record.get_id())) {
						found = true;
						if (record.getRelated() == null) {
							record.setRelated(new HashSet<String>());
						}
						int before = record.getRelated().size();
						record.getRelated().addAll(withouMe);
						log.debug("add related "
								+ (record.getRelated().size() - before));
					}
				}

				// insert new
				if (!found) {
					phones.save(new Phone(adPhone,
							new HashSet<String>(withouMe)));
					log.debug("insert phone " + adPhone);
				}
			}
		} else {
			log.debug("ad has no phones");
		}
	}

	public void save(Iterable<Advert> ads) {
		boolean empty = true;
		if (ads != null) {
			for (Advert ad : ads) {
				empty = false;
				adverts.save(ad);
			}

		}
		if (empty) {
			log.info("There is nothing to save");
		}
	}

	public Iterator<Advert> iterator(boolean processed) {
		return adverts.find(processed ? "" : "{processed:'false'}")
				.as(Advert.class).iterator();
	}

	public void save(Phone phone) {
		phones.save(phone);
	}

	public Iterator<Phone> phoneIterator(boolean forceCheck) {
		return phones.find(forceCheck ? "" : "{checked:'true'}")
				.as(Phone.class).iterator();
	}

	public void shutdown() {
		mongo.close();
		log.debug("mongo client closed");

	}

}
