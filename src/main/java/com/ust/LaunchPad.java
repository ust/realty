package com.ust;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.ust.parsers.FnUa;

public class LaunchPad {
	private static Logger log = LogManager.getLogger(AdvertService.class);
	
	private FnUa parser;
	private AdvertService service;
	
	private String dbName;
	private String imgDir;
	private boolean forceUpdate;
	
	@Before
	public void loadProperties() {
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
	}
	
	@Test
	public void grab() {
		long start = System.currentTimeMillis();
		
		
		try {
			log.info("Preparing...");
			service = new AdvertService();
			parser = new FnUa(service);
			parser.cashe();
			parser.loadProperties();
			parser.connect();			
			
			log.info("Saving all collected links...");
			service.startup(dbName);
			service.save(parser.scan());

			parser.extract(forceUpdate);
			this.check();
			parser.download(imgDir);
			parser.shutdown();
			
		} catch (UnknownHostException e) {
			log.error(e);
			fail("DB problems");
		} catch (IOException e) {
			String msg = "connecting to FN.UA failed";
			log.error(msg, e);
			fail(msg);
		} catch (Exception e) {
			log.error("", e);
			fail("See logs for details");
		}

		log.info("Fn.ua parsed successfully in "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");
	}
	
	private void check() {
		// load unprocessed items
		for (Iterator<Phone> i = service.phoneIterator(true); i.hasNext();) {
			Phone phone = i.next();
			service.save(phone);
			
		}
	}
}
