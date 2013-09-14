package com.ust.servlets;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.ust.parsers.AbstractAdvertParser;

public class Advert extends HttpServlet {
	private static final long serialVersionUID = -3790664628353703374L;
	private static final Logger log = LoggerFactory.getLogger(AbstractAdvertParser.class);

	// cashe
	private MongoCollection ads;
	private Mongo mongoClient;
	private DB db;
	private Jongo jongo;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			mongoClient = new MongoClient();
			db = mongoClient.getDB("test");
			jongo = new Jongo(db);
			ads = jongo.getCollection("advert");

			log.debug("Servlet initialized successfully");
		} catch (UnknownHostException e) {
			log.error("Couldn't connect to DB", e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.debug("Hello! today we have " + ads.count() + " for you");
		
	}

}
