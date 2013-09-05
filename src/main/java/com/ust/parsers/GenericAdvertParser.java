package com.ust.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericAdvertParser implements AdvertParser {
	protected static Logger log = LogManager.getLogger(GenericAdvertParser.class);
	
	protected String configFileName;

	// properties
	protected String scheme;
	protected String host;
	protected String favoriteFilter;
	protected int timeout;
	protected int pageDepth;
	protected String userAgent;

	@Override
	public void configure() {
		Properties props = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(configFileName);
			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (IOException e) {
			log.error("properties \"" + configFileName + "\" couldn't be load",
					e);
		}

		scheme = props.getProperty("scheme");
		log.debug("loaded property \"scheme\" is: " + scheme);

		host = props.getProperty("url");
		log.debug("loaded property \"url\" is: " + host);

		favoriteFilter = props.getProperty("favorite.filter");
		log.debug("loaded property \"favorite\" is: " + favoriteFilter);

		timeout = Integer.parseInt(props.getProperty("timeout"));
		log.debug("loaded property \"timeout\" is: " + timeout);

		pageDepth = Integer.parseInt(props.getProperty("pages.depth"));
		log.debug("loaded property \"pages.depth\" is: " + pageDepth);

		userAgent = props.getProperty("user.agent");
		log.debug("loaded property \"user.agent\" is: " + userAgent);
	}
}
