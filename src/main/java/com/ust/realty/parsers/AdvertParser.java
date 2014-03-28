package com.ust.realty.parsers;

import java.io.IOException;
import java.util.Set;

import com.ust.realty.model.Advert;

public interface AdvertParser {
	
	void configure();

	Set<Advert> scan() throws IOException;

	void extract(boolean updateAll);

	void download(String toDir) throws IOException;

	void close();

}