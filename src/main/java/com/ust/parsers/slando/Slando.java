package com.ust.parsers.slando;

import java.io.IOException;
import java.util.Set;

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

}
