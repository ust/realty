package com.ust.realty.parsers.ua.slando;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Document;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ust.realty.AdvertService;
import com.ust.realty.model.Advert;
import com.ust.realty.parsers.AbstractAdvertParser;

public class Slando extends AbstractAdvertParser {
	private static Logger log = LoggerFactory.getLogger(AbstractAdvertParser.class);

	public Slando(AdvertService service) {
		super(service);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void configure() {
		configFileName = "slando.ua.properties";
		super.configure(getClass());
	}

	@Override
	public Set<Advert> scan() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean parse(Advert ad) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("unused")
	@Test
	public void recognize() {
		String result = null;
		try {
			File root = new File(".");
			File file = new File(root.getCanonicalPath() + "/img/download.png");

			//Tesseract instance = Tesseract.getInstance(); // JNA Interface
															// Mapping
			// Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping
			//result = instance.doOCR(ImageHelper.convertImageToBinary(ImageIO.read(file)));
			log.trace("recognized: " + result);

		} catch (IOException e) {
			log.error("Exception while getting absolute current location", e);
		} //catch (TesseractException e) {
//			log.error("Error while recognition image with numbers", e);
//		}
		// return result;

	}

	// @Test
	@SuppressWarnings("unused")
	public String recognizeWithAsprise() {
		String result = null;
		try {
			File root = new File(".");
			File file = new File(root.getCanonicalPath() + "/img/download.png");

			BufferedImage image = ImageIO.read(file);
			//OCR.setLibraryPath(root.getCanonicalPath() + "/lib/AspriseOCR.dll");

			//result = new OCR().recognizeCharacters(image);

			log.info("RESULTS: \n" + result);

		} catch (IOException e) {
			log.error("", e);
		}
		return result;

	}

	@Override
	protected void collectPhones(Document doc, Advert ad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getPagesCount(Document doc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean is404(Document doc) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
