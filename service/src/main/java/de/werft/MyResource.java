package de.werft;

import de.hpi.rdf.tailrapi.TailrClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource exposed at "/upload".
 * This class handles the file uploads as a restful
 * service. All API calls are documented via swagger.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Path("/upload")
public class MyResource {

    @Inject
    TailrClient tailr;

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response testUploadFile(byte[] fileBytes,
                                   @QueryParam(value = "key") String tailrKey) {

        System.out.println("got " + tailrKey);
        return Response.ok(fileBytes, MediaType.APPLICATION_OCTET_STREAM).build();

    }
}
