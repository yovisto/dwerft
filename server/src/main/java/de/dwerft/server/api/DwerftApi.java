package de.dwerft.server.api;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;

/**
 * 
 * The Dwerft API 
 * 
 * This api provides access to the SPARQL endpoint over.
 * It is divided into three parts.
 * 
 * The first part is provided over open access at the moment.
 * It provides get methods to retrieve certain aspects of the dwerft model.
 * 
 * The second part is secured by a basic authentication.
 * It provides put, post, and delete methods for changing the data model.
 * 
 * The third part is accessable for privileged users only.
 * It provides a basic SPARQL query endpoint.
 * 
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */
@Path("/")
public class DwerftApi {

	@GET
	public Response getMsg(@Auth Subject subject) throws WebApplicationException {
		if (subject.isPermitted("protected:read")) {
			return Response.ok("initit").build();
		} else {
			throw new WebApplicationException(401);
		}
	}
	
	@GET
	@Path("/no")
	public String getM() {
		return "noch an init";
	}
	
	// builds a user forbidden response
	private Response onNotAuthenticated() {
		return Response.status(Status.FORBIDDEN).build();
	}
}
