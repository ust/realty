package com.ust.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ust.AdvertService;
import com.ust.model.Advert;

public abstract class AbstractAdvertParser implements AdvertParser {
	private static Logger log = LogManager
			.getLogger(AbstractAdvertParser.class);

	protected String configFileName;

	// properties
	protected String scheme;
	protected String host;
	protected String listPath;
	protected String itemPath;
	protected String favoriteFilter;
	protected int timeout;
	protected int pageDepth;
	protected String userAgent;
	protected String rowSelector;
	protected String idPattern;
	protected String adTitle;
	protected String adDescription;
	protected String adPrice;
	protected String adImages;
	protected String adDate;
	
	protected AdvertService service;
	protected URIBuilder uriBuilder;
	protected Connection connection;

	protected HashSet<Advert> ads;
	protected boolean cashed;


	public AbstractAdvertParser(AdvertService service) {
		this.service = service;
		this.configure();
	}

	protected void cashe() {
		if (cashed) {
			return;
		}

		uriBuilder = new URIBuilder();
		try {
			URI uri = uriBuilder.setScheme(scheme).setHost(host).setPath(listPath)
					.setQuery(favoriteFilter).build();
			log.trace("uri is " + uri.toString());
			connection = Jsoup.connect(uri.toString()).userAgent(userAgent)
					.timeout(timeout);
		} catch (URISyntaxException e) {
			log.error("Couldn't build valid url");
		}

		cashed = true;

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

		listPath = props.getProperty("list.path");
		log.debug("loaded property \"list.path\" is: " + listPath);
		
		itemPath = props.getProperty("item.path");
		log.debug("loaded property \"item.path\" is: " + listPath);

		favoriteFilter = props.getProperty("favorite.filter");
		log.debug("loaded property \"favorite\" is: " + favoriteFilter);

		timeout = Integer.parseInt(props.getProperty("timeout"));
		log.debug("loaded property \"timeout\" is: " + timeout);

		pageDepth = Integer.parseInt(props.getProperty("pages.depth"));
		log.debug("loaded property \"pages.depth\" is: " + pageDepth);

		userAgent = props.getProperty("user.agent");
		log.debug("loaded property \"user.agent\" is: " + userAgent);

		rowSelector = props.getProperty("row.selector");
		log.debug("loaded property \"row.selector\" is: " + rowSelector);

		adTitle = props.getProperty("title.selector");
		log.debug("loaded property \"title.selector\" is: " + adTitle);
		
		adDescription = props.getProperty("description.selector");
		log.debug("loaded property \"description.selector\" is: " + adDescription);
		
		adPrice = props.getProperty("price.selector");
		log.debug("loaded property \"price.selector\" is: " + adPrice);
		
		adDate = props.getProperty("date.selector");
		log.debug("loaded property \"date.selector\" is: " + adDate);
		
		adImages = props.getProperty("images.selector");
		log.debug("loaded property \"images.selector\" is: " + adImages);		
	}

	@Override
	public Set<Advert> scan() throws IOException {
		cashe();
		log.info("Scanning links in filter...");
		ads = new HashSet<Advert>();
		Document doc = connection.get();

		int pagesCount = getPagesCount(doc);
		log.debug("pages sutisfied filter " + pagesCount);

		outer: for (int i = 1; pageDepth == -1 || i <= pageDepth
				&& i <= pagesCount; connection.data("p", "" + ++i)) {
			doc = connection.get();
			log.debug("page: " + i);

			for (Element e : doc.select(rowSelector)) {
				String url = e.attr("href");
				if (!ads.add(new Advert(getIdFromUrl(url), host, url))) {
					break outer;
				}
				log.trace("found url: " + url);
			}
		}
		return ads.isEmpty() ? null : ads;
	}

	protected long getIdFromUrl(String url) {
		Matcher m = Pattern.compile("(?<=" + idPattern + "=)\\d+").matcher(url);
		m.find();
		return Long.parseLong(m.group());
	}

	protected int getPagesCount(Document doc) {
		return 300;
	}

	@Override
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

	protected boolean parse(Advert ad){
		// parse separate items
		Document doc = null;
		try {
			String url = uriBuilder
					.setPath(itemPath)
					.setQuery(
							URLEncodedUtils.format(Arrays
									.asList(new BasicNameValuePair(idPattern,
											String.valueOf(ad.getId()))),
									"UTF-8")).build().toString();

			int status = connection.url(url).execute().statusCode();
			log.trace("" + status + " from " + url);
			switch (status) {
			case 404:
				log.debug(ad.getId() + " removed");
				ad.setRemoved(true);
				return true;

			case 200:
				doc = connection.get();
				break;

			default:
				log.warn("status unhandled");
				break;
			}
		} catch (URISyntaxException e) {
			log.error("failed to build uri with: " + host + ad.getUrl());
		} catch (IOException e) {
			log.error("failed connection to " + host + ad.getUrl());
			return false;
		}

		// check 404 page XXX: may be redundant
		if (is404(doc)) {
			log.debug(ad.getId() + " removed");
			ad.setRemoved(true);
			return true;
		}

		ad.setTitle(doc.select(adTitle).first().text());
		ad.setDescription(doc.select(adDescription).first().text());
		ad.setPrice(doc.select(adPrice).first().text());
		ad.setDate(doc.select(adDate).first().text());
		// collect images urls
		Elements imgs = doc.select(adImages);
		if (!imgs.isEmpty()) {
			ad.setImgs(new HashSet<String>());
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				ad.getImgs().add(i.next().attr("href"));
			}
		}
		collectPhones(doc, ad);

		log.debug("parsed id " + ad.getId() + " price " + ad.getPrice()
				+ " phones: "
				+ (ad.getPhones() != null ? ad.getPhones().size() : 0)
				+ " images count "
				+ (ad.getImgs() != null ? ad.getImgs().size() : 0));
		return true;
	}
	
	protected boolean is404(Document doc) {
		return "Ошибка 404".equals(doc.select("h2").text());
	}

	protected abstract void collectPhones(Document doc, Advert ad);

	@Override
	public void download(String toDir) throws IOException {
		String currDir = new File("").getCanonicalPath();
		File imgs = new File(currDir + toDir);
		if (!imgs.exists()) {
			imgs.mkdir();
		}
		// download images...
	}

	@Override
	public void close() {
		// do nothing
	}
}
