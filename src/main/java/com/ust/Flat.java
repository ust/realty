package com.ust;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/json/*")
public class Flat extends HttpServlet {
	private static final long serialVersionUID = -7989378549244222826L;

	public static final double latitude = 50.4501;
	public static final double langitude = 30.5234;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		List<Coords> coords = new ArrayList<Coords>();
		for (int i = 0; i < 5; i++) {
			coords.add(new Coords(latitude + Math.random(), langitude
					+ Math.random()));
		}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(new Gson().toJson(coords));
	}
}

class Coords {
	public Coords(double d, double e) {
		lat = d;
		lang = e;
	}

	double lat;
	double lang;
}
