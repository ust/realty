package com.ust;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.ust.model.Phone;
import com.ust.parsers.ua.fn.Fn;
import com.ust.parsers.ua.lun.Lun;

public class LaunchPad {
	private static Logger log = LogManager.getLogger(AdvertService.class);

	private Fn parser;
	private Lun checker;
	private AdvertService service;

	private String dbName;
	private String imgDir;
	private boolean forceUpdate;
	private boolean skipUpdate;
	private boolean forceCheck;

	public static void main(String... args) {
		new LaunchPad().scan().parse().check().print().exit();
	}

	@Test
	public void run() {
		this.configure().scan().parse().check().print().exit();
	}

	public LaunchPad() {
		service = new AdvertService();
	}

	public LaunchPad configure() {
		log.trace("Loading main properties...");
		Properties props = new Properties();
		String resource = "main.properties";
		try {
			InputStream in = getClass().getResourceAsStream(resource);
			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (IOException e) {
			log.error("properties \"" + resource + "\" couldn't be load", e);
		}

		dbName = props.getProperty("db.name");
		log.debug("loaded property \"db.name\" is: " + dbName);

		imgDir = props.getProperty("img.dir");
		log.debug("loaded property \"img.dir\" is: " + imgDir);

		forceUpdate = Boolean.parseBoolean(props.getProperty("foce.update"));
		log.debug("loaded property \"foce.update\" is: " + forceUpdate);

		skipUpdate = Boolean.parseBoolean(props.getProperty("skip.update"));
		log.debug("loaded property \"skip.update\" is: " + skipUpdate);

		forceCheck = Boolean.parseBoolean(props.getProperty("force.check"));
		log.debug("loaded property \"force.check\" is: " + forceCheck);

		if (parser != null) {
			parser.configure();
		}

		return this;
	}

	public LaunchPad parse() {
		long start = System.currentTimeMillis();
		try {
			if (!skipUpdate) {
				if (!service.isRunning()) {
					service.startup(dbName);
				}
				getParser();
				parser.extract(forceUpdate);
				parser.download(imgDir);
			}

		} catch (UnknownHostException e) {
			log.error("DB problems", e);
		} catch (IOException e) {
			log.error("Atempt to connect with FN.UA failed", e);
		} catch (Exception e) {
			log.error("", e);
		}
		log.info("Fn.ua parsed successfully in "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");

		return this;
	}

	public LaunchPad scan() {
		long start = System.currentTimeMillis();
		try {

			log.info("Saving all collected links...");
			if (!service.isRunning()) {
				service.startup(dbName);
			}
			getParser();
			service.save(parser.scan());

		} catch (UnknownHostException e) {
			log.error("DB problems", e);
		} catch (IOException e) {
			log.error("Atempt to connect with FN.UA failed", e);
		} catch (Exception e) {
			log.error("", e);
		}
		log.info("Fn.ua scanned successfully in "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");

		return this;
	}

	public void getParser() {
		if (parser == null) {
			parser = new Fn(service);
			parser.configure();
		}
	}

	public LaunchPad check() {
		if (checker == null) {
			checker = new Lun();
		}

		for (Phone p : service.loadPhones(forceCheck)) {
			try {
				checker.check(p);
				service.save(p);
			} catch (IOException e) {
				log.error("failed to check " + p.getId());
			} catch (RuntimeException e) {
				// TODO: handle exception
			}
		}
		return this;
	}

	public LaunchPad print() {
		Iterator<Phone> i = service.loadPhones(false).iterator();
		if (i.hasNext()) {
			log.info("results, ads wthiout brokers:");
			for (Phone p = i.next(); i.hasNext(); p = i.next()) {
				log.info("	" + p.getAds());
			}
		} else {
			log.info("------- no results :( -------");
		}
		return this;
	}

	public LaunchPad exit() {
		parser.close();
		service.shutdown();
		return this;
	}

}
