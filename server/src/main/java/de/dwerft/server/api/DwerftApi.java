package de.dwerft.server.api;


import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.shiro.authz.annotation.RequiresPermissions;

@Path("/")
public class DwerftApi {

	@GET
	@RequiresPermissions("protected:read")
	public String getMsg() {
		return "Init";
	}
	
	@GET
	@Path("/no")
	public String getM() {
		return "noch an init";
	}
	
}
