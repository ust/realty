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

	public static void main(String... args) {
		new LaunchPad().loadProperties().grab();
	}

	@Test
	public void run() {
		this.loadProperties().grab();
	}

	public LaunchPad loadProperties() {
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

		return this;
	}

	public LaunchPad grab() {
		long start = System.currentTimeMillis();

		try {
			log.info("Preparing...");
			checker = new Lun();
			service = new AdvertService();
			parser = new Fn(service);
			parser.cashe();
			parser.loadProperties();
			parser.connect();

			log.info("Saving all collected links...");
			service.startup(dbName);
			service.save(parser.scan());

			if (!skipUpdate) {
				parser.extract(forceUpdate);
			}
			this.check();
			parser.download(imgDir);
			parser.shutdown();

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

	private void check() {
		for (Iterator<Phone> i = service.phoneIterator(true); i.hasNext();) {
			Phone p = i.next();
			try {
				p.setBroker(checker.check(p.get_id()));
				p.setChecked(true);
				service.save(p);
			} catch (IOException e) {
				// TODO try to truncate leading zero number
				log.error("failed to check " + p.get_id());
			}
		}
	}

}
