package com.ust;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/flats")
public class Flat
{
    @GET
    @Produces("text/plain")
    public String getStub()
    {
        return "hello";
    }
}
