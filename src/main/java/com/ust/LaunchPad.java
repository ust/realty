package com.ust;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ust.model.Phone;
import com.ust.parsers.AdvertParser;
import com.ust.parsers.ua.aviso.Aviso;
import com.ust.parsers.ua.lun.Lun;

public class LaunchPad {
	private static Logger log = LoggerFactory.getLogger(LaunchPad.class);

	private List<AdvertParser> parsers;
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
		log.debug("loaded property \"db.name\" is: ", dbName);

		imgDir = props.getProperty("img.dir");
		log.debug("loaded property \"img.dir\" is: ", imgDir);

		forceUpdate = Boolean.parseBoolean(props.getProperty("foce.update"));
		log.debug("loaded property \"foce.update\" is: ", forceUpdate);

		skipUpdate = Boolean.parseBoolean(props.getProperty("skip.update"));
		log.debug("loaded property \"skip.update\" is: ", skipUpdate);

		forceCheck = Boolean.parseBoolean(props.getProperty("force.check"));
		log.debug("loaded property \"force.check\" is: ", forceCheck);

		if (parsers != null) {
			for (AdvertParser p : parsers) {
				p.configure();
			}
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
				getParsers();
				for (AdvertParser p : parsers) {
					p.extract(forceUpdate);
					p.download(imgDir);
				}
			}

		} catch (UnknownHostException e) {
			log.error("DB problems", e);
		} catch (IOException e) {
			log.error("Atempt to connect to donar failed", e);
		} catch (Exception e) {
			log.error("", e);
		}
		log.info("Fn.ua parsed successfully in {} seconds",
				(System.currentTimeMillis() - start) / 1000);
		return this;
	}

	public LaunchPad scan() {
		long start = System.currentTimeMillis();
		try {

			log.info("Saving all collected links...");
			if (!service.isRunning()) {
				service.startup(dbName);
			}
			getParsers();
			for (AdvertParser p : parsers) {
				service.save(p.scan());
			}

		} catch (UnknownHostException e) {
			log.error("DB problems", e);
		} catch (IOException e) {
			log.error("Atempt to connect with FN.UA failed", e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Fn.ua scanned successfully in {} seconds",
				(System.currentTimeMillis() - start) / 1000);

		return this;
	}

	public void getParsers() {
		if (parsers == null) {
			parsers = new ArrayList<AdvertParser>();
			// parsers.add(new Fn(service));
			parsers.add(new Aviso(service));
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
				log.error("failed to check {}", p.getId());
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
				log.info("	{}", p.getAds());
			}
		} else {
			log.info("------- no results :( -------");
		}
		return this;
	}

	public LaunchPad exit() {
		for (AdvertParser p : parsers) {
			p.close();
		}
		service.shutdown();
		return this;
	}

}
