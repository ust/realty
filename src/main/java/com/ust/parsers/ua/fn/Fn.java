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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.ust.AdvertService;
import com.ust.model.Advert;
import com.ust.parsers.AbstractAdvertParser;

public class Fn extends AbstractAdvertParser {
	private static Logger log = LoggerFactory.getLogger(Fn.class);

	private HttpClient httpClient;
	private Gson gson;

	public Fn(AdvertService service) {
		super(service);
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
	public void configure() {
		log.trace("Loading fn.ua parser properties...");
		configFileName = "fn.ua.properties";
		super.configure();
	}

	@Override
	protected int getPagesCount(Document doc) {
		return Integer.parseInt(doc.select(".pages b").first().text()
				.split("из")[1].replaceAll("\\D", ""));
	}

	@Override
	protected void collectPhones(Document doc, Advert ad) {
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
				log.trace("number {}", phone);
			}
			ad.setPhones(new HashSet<String>(phones));
		}
	}

	@Override
	public void close() {
		httpClient.getConnectionManager().shutdown();
		log.debug("http client closed");
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
			log.debug("uri encoded: {}", uri);

			// extract numbers from response
			numbers = gson.fromJson(
					new InputStreamReader(httpClient.execute(request)
							.getEntity().getContent()), PhoneResponse.class).items;

		} catch (UnsupportedEncodingException e) {
			log.error("Incorrect encoding: " + e.getMessage(), e);
		} catch (ClientProtocolException e) {
			log.error(
					"Got http error while trying get phone number: "
							+ e.getMessage(), e);
		} catch (IOException e) {
			log.error(
					"Interrupted atempt to get phone number: " + e.getMessage(),
					e);
		} catch (URISyntaxException e) {
			log.error("Failed to encode request url: " + e.getMessage(), e);
		}
		return numbers;
	}
}

class PhoneResponse {
	Map<String, String> items;
}