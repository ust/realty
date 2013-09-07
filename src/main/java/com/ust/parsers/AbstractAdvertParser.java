package com.ust.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.ust.AdvertService;
import com.ust.model.Advert;

public abstract class AbstractAdvertParser implements AdvertParser {
	protected static Logger log = LogManager
			.getLogger(AbstractAdvertParser.class);

	protected String configFileName;

	// properties
	protected String scheme;
	protected String host;
	protected String favoriteFilter;
	protected int timeout;
	protected int pageDepth;
	protected String userAgent;

	protected AdvertService service;
	protected URIBuilder uriBuilder;
	protected Connection connection;

	protected HashSet<Advert> ads;
	protected boolean cashed;

	public AbstractAdvertParser(AdvertService service) {
		this.service = service;
		this.configure();
	}

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

	public void extract(boolean updateAll) {
		cashe();

		// load unprocessed items
		for (Advert ad : service.load(updateAll)) {
			if (parse(ad)) {
				ad.setProcessed(true);
				service.save(ad);
			}
		}
	}

	public void download(String toDir) throws IOException {
		String currDir = new File("").getCanonicalPath();
		File imgs = new File(currDir + toDir);
		if (!imgs.exists()) {
			imgs.mkdir();
		}
		// download images...
	}

	protected abstract boolean parse(Advert ad);

	protected void cashe() {
		uriBuilder = new URIBuilder();
		try {
			URI uri = uriBuilder.setScheme(scheme).setHost(host)
					.setPath("listing_list.php").setQuery(favoriteFilter)
					.build();
			log.trace("uri is " + uri.toString());
			connection = Jsoup.connect(uri.toString()).userAgent(userAgent)
					.timeout(timeout);
		} catch (URISyntaxException e) {
			log.error("Couldn't build valid url");
		}

	}
}
