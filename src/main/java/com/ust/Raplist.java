package com.ust;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.google.gson.Gson;
import com.mongodb.Mongo;

public class Raplist extends HttpServlet {
	private static final long serialVersionUID = -5200533547601694317L;

	private static final String DB = "test";
	private static final String COLLECTION = "test";

	private MongoCollection brokers;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			brokers = new Jongo(new Mongo().getDB(DB))
					.getCollection(COLLECTION);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String number = req.getParameter("number");
		Broker b = brokers.findOne("{number:'" + number + "'}")
				.as(Broker.class);
		
		b = (b != null) ? b : new Broker(); 

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(new Gson().toJson(b));
	}

	class Broker {
		String name = "no name";
		String number = "no number";
		List<String> comments = Arrays.asList("no comments");
	}

}
