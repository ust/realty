package com.ust.parsers;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.ust.Advert;
import com.ust.AdvertService;

public class FnUa {

	private static Logger log = LogManager.getLogger(FnUa.class);

	// properties
	private String scheme;
	private String host;
	private String kievAll;
	private String favoriteFilter;
	private int timeout;
	private String dbName;
	private String imgDir;
	private int topLim;
	private boolean forceUpdate;
	// cashe
	private URIBuilder uriBuilder;
	private HttpClient httpClient;

	private AdvertService service;
	private Connection con;
	private Gson gson;

	private HashSet<Advert> ads;

	@Before
	public void cashing() {
		service = new AdvertService();
		uriBuilder = new URIBuilder();
		httpClient = new DefaultHttpClient();
		gson = new Gson();
	}

	@Before
	public void loadProperties() {
		log.trace("Loading properties...");
		Properties props = new Properties();
		String resource = "fn.ua.properties";
		try {
			InputStream in = FnUa.class.getResourceAsStream(resource);
			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (IOException e) {
			log.error("properties \"" + resource + "\" couldn't be load", e);
		}

		scheme = props.getProperty("scheme");
		log.debug("loaded property \"scheme\" is: " + scheme);

		host = props.getProperty("url");
		log.debug("loaded property \"url\" is: " + host);

		kievAll = props.getProperty("kiev.all");
		log.debug("loaded property \"kiev.all\" is: " + kievAll);

		favoriteFilter = props.getProperty("favorite.filter");
		log.debug("loaded property \"favorite\" is: " + favoriteFilter);

		dbName = props.getProperty("db.name");
		log.debug("loaded property \"db.name\" is: " + dbName);

		imgDir = props.getProperty("img.dir");
		log.debug("loaded property \"img.dir\" is: " + imgDir);

		timeout = Integer.parseInt(props.getProperty("timeout"));
		log.debug("loaded property \"timeout\" is: " + timeout);

		topLim = Integer.parseInt(props.getProperty("pages.depth"));
		log.debug("loaded property \"pages.depth\" is: " + topLim);

		forceUpdate = Boolean.parseBoolean(props.getProperty("foce.update"));
		log.debug("loaded property \"foce.update\" is: " + forceUpdate);
	}

	@After
	public void shutdown() {
		service.shutdown();

		httpClient.getConnectionManager().shutdown();
		log.debug("http client closed");
	}

	@Test
	public void grab() {
		long start = System.currentTimeMillis();

		try {
			connectToSite();

			log.info("Scanning links in filter...");
			scan();

			log.info("Saving all collected links...");
			service.startup(dbName);
			service.save(ads);

			extract();
			download();
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

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	private void connectToSite() throws URISyntaxException {
		log.trace("Connecting to fn.ua...");
		// XXX separate path from query parameters
		URI uri = uriBuilder.setScheme(scheme).setHost(host)
				.setQuery(favoriteFilter).build();
		con = Jsoup.connect(uri.toString()).timeout(timeout);
	}

	private void scan() throws IOException {
		ads = new HashSet<Advert>();
		Document doc = con.get();
		// XXX doing by page counter neither duplicate occurrence
		outer: for (int i = 1; i <= topLim; con.data("p", "" + ++i)) {
			doc = con.get();
			log.debug("page: " + i);

			for (Element e : doc.select(".offer-photo a")) {
				String url = e.attr("href");
				if (!ads.add(new Advert(url))) {
					break outer;
				}
				log.trace("find url: " + url);
			}
		}
	}

	private void extract() {
		// load unprocessed items
		for (Iterator<Advert> i = service.iterator(forceUpdate); i.hasNext();) {
			Advert ad = i.next();

			// save to appropriate file
			if (parse(ad)) {
				ad.setProcessed(true);
				service.save(ad);
			}
		}
	}

	private boolean parse(Advert ad) {
		// parse separate items
		Document doc = null;
		try {
			// TODO
			String url = uriBuilder
					.setPath("view.php")
					.setQuery(
							URLEncodedUtils.format(Arrays
									.asList(new BasicNameValuePair("ad_id",
											String.valueOf(ad.get_id()))),
									"UTF-8")).build().toString();
			log.trace("connecting to " + url);
			doc = con.url(url).get();
		} catch (IOException e) {
			log.error("failed connection to " + host + ad.getUrl());
			return false;
		} catch (URISyntaxException e) {
			log.error("failed to build uri with: " + host + ad.getUrl());
		}
		ad.setTitle(doc.select("h1").text());
		ad.setDescription(doc.select("p.ad-desc").text());
		ad.setPrice(doc.select("p.ad-price b").text());
		ad.setDate(doc.select("p.ad-pub-date b").first().text());
		// collect images urls
		Elements imgs = doc.select("#ad-thumbs a.highslide");
		if (!imgs.isEmpty()) {
			ad.setImgs(new ArrayList<String>());
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				ad.getImgs().add(i.next().attr("href"));
			}
		}
		// collect phone numbers
		Map<String, String> numbers = requestNumbers(
				"fn_rubrics_menu/backendTest.php", ad.get_id(),
				doc.select("#show-phone").attr("data-hash"));
		if (numbers != null && numbers.size() > 0) {
			ad.setNumbers(new ArrayList<String>());
			for (Iterator<Element> i = doc.select("p.ad-contacts b span")
					.iterator(); i.hasNext();) {
				Element e = i.next();
				ad.getNumbers().add(
						e.parent().ownText().replaceAll("\\D", "")
								+ numbers.get("aphone"
										+ (ad.getNumbers().size() + 1)));
				log.trace("number : "
						+ ad.getNumbers().get(ad.getNumbers().size() - 1));
			}
		}

		log.debug("parsed id " + ad.get_id() + " price " + ad.getPrice()
				+ " phones: "
				+ (ad.getNumbers() != null ? ad.getNumbers().size() : 0)
				+ " images count "
				+ (ad.getImgs() != null ? ad.getImgs().size() : 0));
		return true;
	}

	private Map<String, String> requestNumbers(String url, final long id,
			final String hash) {
		Map<String, String> numbers = null;
		try {
			// FIXME setup request
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost(host)
					.setPath(url)
					.setQuery(
							URLEncodedUtils.format(Arrays.asList(
									new BasicNameValuePair("serverData", ""
											+ id), new BasicNameValuePair(
											"hashphone", hash),
									new BasicNameValuePair("serviceName",
											"showphone")), "UTF-8")).build();
			HttpUriRequest request = new HttpGet(uri);
			request.setHeader("Content-Type", "text/html");
			request.setHeader("X-Requested-With", "XMLHttpRequest");
			log.debug("uri encoded: " + uri);

			// extract numbers from response
			InputStreamReader stream = new InputStreamReader(httpClient
					.execute(request).getEntity().getContent());
			// log.trace(IOUtils.toString(stream));

			PhoneResponse data = gson.fromJson(stream, PhoneResponse.class);
			numbers = data.items;

		} catch (UnsupportedEncodingException e) {
			log.error("Incorrect encoding: ", e);
		} catch (ClientProtocolException e) {
			log.error("Got http error while trying get phone number: ", e);
		} catch (IOException e) {
			log.error("Interrupted atempt to get phone number: ", e);
		} catch (URISyntaxException e) {
			log.error("Failed to encode request url: ", e);
		}
		return numbers;
	}

	private void download() throws IOException {
		String currDir = new File("").getCanonicalPath();
		File imgs = new File(currDir + imgDir);
		if (!imgs.exists()) {
			imgs.mkdir();
		}
		// download images...
	}

}

class PhoneRequest {
	final String serviceName = "showphone";

	long serverData;
	String hashphone;

	PhoneRequest(long id, String hash) {
		serverData = id;
		hashphone = hash;
	}

	PhoneRequest() {
	}
}

class PhoneResponse {
	Map<String, String> items;
}
