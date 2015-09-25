package de.dwerft.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api")
public class DwerftApi {

	@GET
	public String getMsg() {
		return "Init";
	}
	
}
