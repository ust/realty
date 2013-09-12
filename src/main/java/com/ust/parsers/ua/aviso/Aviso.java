package com.ust.parsers.ua.aviso;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.ust.AdvertService;
import com.ust.model.Advert;
import com.ust.parsers.AbstractAdvertParser;

public class Aviso extends AbstractAdvertParser {
	private static Logger log = LogManager.getLogger(Aviso.class);

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
	protected boolean parse(Advert ad) {
		// TODO Auto-generated method stub
		return false;
	}

}
