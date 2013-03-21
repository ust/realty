package com.ust;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.google.gson.Gson;
import com.mongodb.Mongo;

@WebServlet("/json/*")
public class Flat extends HttpServlet {
	private static final long serialVersionUID = -7989378549244222826L;

	public static final double LAT = 50.4501;
	public static final double LNG = 30.5234;

    private Jongo jongo;
    private MongoCollection collection;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            jongo = new Jongo(new Mongo().getDB("test"));
            collection = jongo.getCollection("test");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
        // List<Coords> coords = new ArrayList<Coords>();
        // for (int i = 0; i < 5; i++) {
        // coords.add(new Coords(LAT + Math.random()/10, LNG
        // + Math.random()/10));
        // }

        List<Coords> coords = new ArrayList<Coords>();
        System.out.println("count loaded " + collection.count());
        int i = 0;
        for (Coords c : collection.find().as(Coords.class))
        {
            System.out.println("loaded " + i++);
            coords.add(c);
        }

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		// TODO replace with Jackson
        resp.getWriter().write(new Gson().toJson(coords));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

        Double lat = Double.valueOf((String) req.getParameter("lat"));
        Double lng = Double.valueOf((String) req.getParameter("lng"));

        System.out.println("recive new point [lat: " + lat + "lang: " + lng
                + "]");

        Coords c = new Coords(lat, lng);
        collection.save(c);

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("saved");
	}
}

class Coords
{
    public Coords()
    {
    }
    public Coords(double d, double e)
    {
        lat = d;
        lang = e;
    }

    double lat;

    double lang;
}
