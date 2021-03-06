package com.ust.realty;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.ust.realty.model.Advert;
import com.ust.realty.model.Phone;

public class AdvertService {
	private static Logger log = LoggerFactory.getLogger(AdvertService.class);

	private MongoClient mongo;
	private Jongo jongo;
	private MongoCollection adverts;
	private MongoCollection phones;
	private boolean running;

	public void startup(String dbName) throws UnknownHostException {
		if (running) {
			throw new IllegalStateException(
					"Service is already in running state");
		}
		mongo = new MongoClient();
		jongo = new Jongo(mongo.getDB(dbName));
		adverts = jongo.getCollection("adverts");
		phones = jongo.getCollection("phones");
		running = true;

		log.debug("mongo client started up");

	}

	public void save(Advert ad) {
		adverts.save(ad);
		log.debug("ad \\w id:" + ad.getId() + " saved");

		Set<String> adPhones = ad.getPhones();
		if (adPhones != null && !adPhones.isEmpty()) {
			// query existing phones
			String query = "{field:{$in:" + adPhones + "}}";
			log.trace("quering as " + query);
			Iterable<Phone> records = phones.find(query).as(Phone.class);

			for (String adPhone : adPhones) {
				boolean found = false;
				Set<String> withouMe = new HashSet<String>(adPhones);
				withouMe.remove(adPhone);

				// update records in db
				for (Iterator<Phone> i = records.iterator(); i.hasNext();) {
					Phone record = i.next();
					if (adPhone.equals(record.getId())) {
						found = true;
						if (record.getAds() == null) {
							record.setAds(new HashSet<String>());
						}
						record.getAds().add(String.valueOf(ad.getId()));

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
					phones.save(new Phone(adPhone, String.valueOf(ad.getId()),
							new HashSet<String>(withouMe)));
					log.debug("insert phone " + adPhone);
				}
			}
		} else {
			log.debug("no phones in advert");
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

	public Iterable<Advert> load(boolean processed) {
		return adverts.find(processed ? "" : "{processed:false}").as(
				Advert.class);
	}

	public void save(Phone phone) {
		phones.save(phone);
	}

	public Iterable<Phone> loadPhones(boolean forceCheck) {
		return phones.find(forceCheck ? "" : "{broker:'false'}")
				.as(Phone.class);
	}

	public void shutdown() {
		if (!running) {
			throw new IllegalStateException("Service is not in running state");
		}
		mongo.close();
		log.debug("mongo client closed");

	}

	public boolean isRunning() {
		return running;
	}

}
