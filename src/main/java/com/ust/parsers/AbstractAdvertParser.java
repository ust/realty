package com.ust.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ust.AdvertService;
import com.ust.model.Advert;
import com.ust.parsers.ua.aviso.Aviso;
import com.ust.parsers.ua.fn.Fn;

//@XmlRootElement(name = "parser")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(value = { Fn.class, Aviso.class })
public abstract class AbstractAdvertParser implements AdvertParser {
	private static Logger log = LoggerFactory
			.getLogger(AbstractAdvertParser.class);

	protected String configFileName;

	// properties
	@XmlElement(name = "scheme", defaultValue = "http")
	protected String scheme;
	@XmlAttribute(name = "host")
	protected String host;
	@XmlElement(name = "list-path")
	protected String listPath;
	@XmlElement(name = "item-path")
	protected String itemPath;
	@XmlElement(name = "filter")
	protected String favoriteFilter;
	@XmlAttribute(name = "timeout")
	protected int timeout;
	@XmlAttribute(name = "depth")
	protected int pageDepth;
	@XmlElement(name = "user-agent")
	protected String userAgent;

	// selectors & patterns
	@XmlElement(name = "id-param-name")
	protected String idParamName;
	@XmlElement(name = "selector-row")
	protected String rowSelector;
	@XmlElement(name = "selector-title")
	protected String titleSelector;
	// @XmlElement(name = "pattern-title")
	protected String titlePattern;
	@XmlElement(name = "selector-description")
	protected String descriptionSelector;
	// @XmlElement(name = "pattern-description")
	protected String descriptionPattern;
	@XmlElement(name = "selector-price")
	protected String priceSelector;
	@XmlElement(name = "pattern-price")
	protected String pricePattern;
	@XmlElement(name = "selector-images")
	protected String imagesSelector;
	// @XmlElement(name = "pattern-images")
	protected String imagesPattern;
	@XmlElement(name = "selector-date")
	protected String dateSelector;
	// @XmlElement(name = "pattern-date")
	protected String datePattern;

	protected AdvertService service;
	protected URIBuilder uriBuilder;
	protected Connection connection;

	protected HashSet<Advert> ads;
	protected boolean cashed;

	public AbstractAdvertParser() {
		// unmarshalling
	}

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
			URI uri = uriBuilder.setScheme(scheme).setHost(host)
					.setPath(listPath).setQuery(favoriteFilter).build();
			log.trace("uri is " + uri.toString());
			connection = Jsoup.connect(uri.toString()).userAgent(userAgent)
					.timeout(timeout);
		} catch (URISyntaxException e) {
			log.error("Couldn't build valid url");
		}

		cashed = true;

	}

	public void configure(Class<? extends AdvertParser> clazz) {
		try {
			// unmarshall parser
			AbstractAdvertParser config = (AbstractAdvertParser) JAXBContext
					.newInstance(AbstractAdvertParser.class, clazz)
					.createUnmarshaller()
					.unmarshal(getClass().getResourceAsStream(configFileName));

			// copy generic parser fields
			copyFileds(AbstractAdvertParser.class, config);
			// copy concrete parser fields
			copyFileds(clazz, config);

		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void copyFileds(Class<?> clazz, Object instance) {
		for (Field field : clazz.getDeclaredFields()) {
			// find field with attribute or element annotation
			String name = null;
			XmlElement element = field.getAnnotation(XmlElement.class);
			if (element != null) {
				name = element.name();
			} else {
				XmlAttribute attribute = field
						.getAnnotation(XmlAttribute.class);
				if (attribute != null) {
					name = attribute.name();
				} else {
					continue;
				}
			}
			// set unmarshalled value to this instance
			field.setAccessible(true);
			try {
				field.set(this, field.get(instance));
				log.info("property {} assigned with value {}", name,
						field.get(instance));

			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
			field.setAccessible(false);
		}
	}

	@Override
	public Set<Advert> scan() throws IOException {
		cashe();
		log.info("Scanning links in filter...");
		ads = new HashSet<Advert>();
		Document doc = connection.get();

		int pagesCount = getPagesCount(doc);
		log.debug("pages sutisfied filter {}", pagesCount);

		outer: for (int i = 1; pageDepth == -1 || i <= pageDepth
				&& i <= pagesCount; connection.data("p", "" + ++i)) {
			doc = connection.get();
			log.debug("page {}", i);

			for (Element e : doc.select(rowSelector)) {
				String url = e.attr("href");
				if (!ads.add(new Advert(getIdFromUrl(url), host, url))) {
					break outer;
				}
				log.trace("found url {}", url);
			}
		}
		return ads.isEmpty() ? null : ads;
	}

	protected long getIdFromUrl(String url) {
		Matcher m = Pattern.compile("(?<=" + idParamName + "=)\\d+").matcher(
				url);
		m.find();
		return Long.parseLong(m.group());
	}

	protected abstract int getPagesCount(Document doc);

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

	protected boolean parse(Advert ad) {
		// parse separate items
		Document doc = null;
		try {
			String url = uriBuilder
					.setPath(itemPath)
					.setQuery(
							URLEncodedUtils.format(Arrays
									.asList(new BasicNameValuePair(idParamName,
											String.valueOf(ad.getId()))),
									"UTF-8")).build().toString();

			int status = connection.url(url).execute().statusCode();
			log.trace("{} from {}", status, url);
			switch (status) {
			case 404:
				log.debug("{} removed", ad.getId());
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
			log.error("failed to build uri of {} {}", host, ad.getUrl());
		} catch (IOException e) {
			log.error("failed connection to {} {}", host, ad.getUrl());
			return false;
		}

		// check 404 page XXX: may be redundant
		if (is404(doc)) {
			log.debug("{} removed", ad.getId());
			ad.setRemoved(true);
			return true;
		}

		ad.setTitle(extractField(doc, titleSelector, ""));
		ad.setDescription(extractField(doc, descriptionSelector, ""));
		ad.setPrice(extractField(doc, priceSelector, ""));
		ad.setDate(extractField(doc, dateSelector, ""));
		// collect images urls
		Elements imgs = doc.select(imagesSelector);
		if (!imgs.isEmpty()) {
			ad.setImgs(new HashSet<String>());
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				ad.getImgs().add(i.next().attr("href"));
			}
		}
		collectPhones(doc, ad);

		log.debug("parsed id " + ad.getId() + " price " + ad.getPrice()
				+ " phones {} images count {}", ad.getImgs() != null ? ad
				.getImgs().size() : 0, ad.getPhones() != null ? ad.getPhones()
				.size() : 0);
		return true;
	}

	protected abstract boolean is404(Document doc);

	private String extractField(Document doc, String selector, String regex) {
		String result = null;
		Elements e = doc.select(selector);
		if (e != null && !e.isEmpty()) {
			// apply pattern if it is specified
			if (StringUtils.isNotBlank(regex)) {
				String text = e.first().text();
				Matcher m = Pattern.compile(regex).matcher(text);
				m.find();
				result = m.group();

				if (StringUtils.isBlank(result)) {
					log.error("No matches in {} by {}", text, regex);
				}
			} else {
				result = e.first().text();
			}
		}
		if (StringUtils.isEmpty(result)) {
			log.error("nothing found by '{}' selector", selector);
		}
		return result;
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

	public void setService(AdvertService service) {
		this.service = service;
	}

	@SuppressWarnings("unused")
	private void loadProperties() {
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

		idParamName = props.getProperty("id.pattern");
		log.debug("loaded property \"id.pattern\" is: " + idParamName);

		titleSelector = props.getProperty("title.selector");
		log.debug("loaded property \"title.selector\" is: " + titleSelector);

		descriptionSelector = props.getProperty("description.selector");
		log.debug("loaded property \"description.selector\" is: "
				+ descriptionSelector);

		priceSelector = props.getProperty("price.selector");
		log.debug("loaded property \"price.selector\" is: " + priceSelector);

		dateSelector = props.getProperty("date.selector");
		log.debug("loaded property \"date.selector\" is: " + dateSelector);

		imagesSelector = props.getProperty("images.selector");
		log.debug("loaded property \"images.selector\" is: " + imagesSelector);
	}
}
