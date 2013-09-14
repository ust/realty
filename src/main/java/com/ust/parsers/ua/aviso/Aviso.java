package com.ust.parsers.ua.aviso;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ust.AdvertService;
import com.ust.model.Advert;
import com.ust.parsers.AbstractAdvertParser;

public class Aviso extends AbstractAdvertParser {
	private static Logger log = LoggerFactory.getLogger(AbstractAdvertParser.class);

	public Aviso(AdvertService service) {
		super(service);
	}

	@Override
	public void configure() {
		log.trace("Loading fn.ua parser properties...");
		configFileName = "aviso.ua.properties";
		super.configure();
	}

	@Override
	protected int getPagesCount(Document doc) {
		return Integer.parseInt(doc.select("span.bold_orange").last().text());
	}

	@Override
	protected void collectPhones(Document doc, Advert ad) {
		// TODO	impl
	}

	@Override
	protected boolean is404(Document doc)  {
		// TODO	impl
		return false;
	}

}
