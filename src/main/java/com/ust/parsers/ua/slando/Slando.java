package com.ust.parsers.ua.slando;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageHelper;

import org.junit.Test;

import com.asprise.util.ocr.OCR;
import com.ust.model.Advert;
import com.ust.parsers.GenericAdvertParser;

public class Slando extends GenericAdvertParser {

	@Override
	public void configure() {
		configFileName = "slando.ua.properties";
		super.configure();
	}

	@Override
	public Set<Advert> scan() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void extract(boolean updateAll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void download(String toDir) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Test
	public void recognize() {
		String result = null;
		try {
			File root = new File(".");
			File file = new File(root.getCanonicalPath() + "/img/download.png");

			Tesseract instance = Tesseract.getInstance(); // JNA Interface
															// Mapping
			// Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping
			result = instance.doOCR(ImageHelper.convertImageToBinary(ImageIO
					.read(file)));
			log.trace("recognized: " + result);

		} catch (IOException e) {
			log.error("Exception while getting absolute current location", e);
		} catch (TesseractException e) {
			log.error("Error while recognition image with numbers", e);
		}
		// return result;

	}

	// @Test
	public String recognizeWithAsprise() {
		String result = null;
		try {
			File root = new File(".");
			File file = new File(root.getCanonicalPath() + "/img/download.png");

			BufferedImage image = ImageIO.read(file);
			OCR.setLibraryPath(root.getCanonicalPath() + "/lib/AspriseOCR.dll");

			result = new OCR().recognizeCharacters(image);

			log.info("RESULTS: \n" + result);

		} catch (IOException e) {
			log.error("", e);
		}
		return result;

	}
}
