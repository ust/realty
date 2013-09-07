package com.ust.parsers.ua.fn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.ust.AdvertService;
import com.ust.model.Advert;
import com.ust.parsers.AbstractAdvertParser;

public class Fn extends AbstractAdvertParser {

	private static Logger log = LogManager.getLogger(Fn.class);

	private HttpClient httpClient;
	private Gson gson;

	public Fn(AdvertService service) {
		super(service);
	}

	@Override
	public void configure() {
		log.trace("Loading fn.ua parser properties...");
		configFileName = "fn.ua.properties";
		super.configure();
	}

	@Override
	public Set<Advert> scan() throws IOException {
		cashe();
		log.info("Scanning links in filter...");
		ads = new HashSet<Advert>();
		Document doc = connection.get();

		int pagesCount = Integer.parseInt(doc.select(".pages b").first().text()
				.split("из")[1].replaceAll("\\D", ""));
		log.debug("pages sutisfied filter " + pagesCount);

		outer: for (int i = 1; pageDepth == -1 || i <= pageDepth
				&& i <= pagesCount; connection.data("p", "" + ++i)) {
			doc = connection.get();
			log.debug("page: " + i);

			for (Element e : doc.select(".offer-photo a")) {
				String url = e.attr("href");
				if (!ads.add(new Advert(url))) {
					break outer;
				}
				log.trace("find url: " + url);
			}
		}
		return ads.isEmpty() ? null : ads;
	}

	@Override
	public void close() {
		httpClient.getConnectionManager().shutdown();
		log.debug("http client closed");
	}

	@Override
	protected void cashe() {
		if (cashed) {
			return;
		}
		super.cashe();
		httpClient = new DefaultHttpClient();
		gson = new Gson();
	}

	@Override
	protected boolean parse(Advert ad) {
		// parse separate items
		Document doc = null;
		try {
			String url = uriBuilder
					.setPath("view.php")
					.setQuery(
							URLEncodedUtils.format(Arrays
									.asList(new BasicNameValuePair("ad_id",
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
		if ("Ошибка 404".equals(doc.select("h2").text())) {
			log.debug(ad.getId() + " removed");
			ad.setRemoved(true);
			return true;
		}

		ad.setTitle(doc.select("h1").text());
		ad.setDescription(doc.select("p.ad-desc").text());
		ad.setPrice(doc.select("p.ad-price b").text());
		ad.setDate(doc.select("p.ad-pub-date b").first().text());
		// collect images urls
		Elements imgs = doc.select("#ad-thumbs a.highslide");
		if (!imgs.isEmpty()) {
			ad.setImgs(new HashSet<String>());
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				ad.getImgs().add(i.next().attr("href"));
			}
		}
		// collect phone numbers
		Map<String, String> numbers = requestNumbers(
				"fn_rubrics_menu/backendTest.php", ad.getId(),
				doc.select("#show-phone").attr("data-hash"));

		// aggregate provider codes with numbers
		if (numbers != null && numbers.size() > 0) {
			ArrayList<String> phones = new ArrayList<String>();
			for (Iterator<Element> i = doc.select("p.ad-contacts b span")
					.iterator(); i.hasNext();) {
				Element e = i.next();

				// extract
				String providerCode = e.parent().ownText()
						.replaceAll("\\D", "");
				// truncate possible leading zeros
				if (providerCode.length() == 4 && providerCode.startsWith("0")) {
					providerCode = providerCode.substring(1);
				}
				// order is important
				String phone = providerCode
						+ numbers.get("aphone" + (phones.size() + 1));

				phones.add(phone);
				log.trace("number : " + phone);
			}
			ad.setPhones(new HashSet<String>(phones));
		}

		log.debug("parsed id " + ad.getId() + " price " + ad.getPrice()
				+ " phones: "
				+ (ad.getPhones() != null ? ad.getPhones().size() : 0)
				+ " images count "
				+ (ad.getImgs() != null ? ad.getImgs().size() : 0));
		return true;
	}

	private Map<String, String> requestNumbers(String url, final long id,
			final String hash) {
		Map<String, String> numbers = null;
		try {
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
			numbers = gson.fromJson(
					new InputStreamReader(httpClient.execute(request)
							.getEntity().getContent()), PhoneResponse.class).items;

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
}

class PhoneResponse {
	Map<String, String> items;
}