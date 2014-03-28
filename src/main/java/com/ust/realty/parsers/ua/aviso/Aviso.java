package com.ust.realty.parsers.ua.aviso;

import javax.xml.bind.annotation.XmlRootElement;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ust.realty.AdvertService;
import com.ust.realty.model.Advert;
import com.ust.realty.parsers.AbstractAdvertParser;

@XmlRootElement(name = "aviso")
public class Aviso extends AbstractAdvertParser {
	private static Logger log = LoggerFactory
			.getLogger(AbstractAdvertParser.class);

	public Aviso() {
		// unmarshalling
	}

	public Aviso(AdvertService service) {
		super(service);
	}

	@Override
	public void configure() {
		log.trace("Loading aviso.ua parser config...");
		configFileName = "aviso.ua.xml";
		super.configure(getClass());
	}

	@Override
	protected int getPagesCount(Document doc) {
		return Integer.parseInt(doc.select(".pull-right .bold_orange").last().text());
	}

	@Override
	protected void collectPhones(Document doc, Advert ad) {
		// TODO impl
	}

	@Override
	protected boolean is404(Document doc) {
		// TODO impl
		return false;
	}

}
