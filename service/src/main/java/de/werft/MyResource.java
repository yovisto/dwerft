package de.werft;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/upload")
@Api(value = "stores", authorizations = {
        @Authorization(value = "username+passphrase", scopes = {})
})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt() {
        return "Got it!";
    }
}
