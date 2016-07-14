package de.werft;

import de.hpi.rdf.tailrapi.Tailr;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
    ServiceConfig conf;

    @Inject
    Tailr tailrClient;

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response testUploadFile(byte[] fileBytes,
                                   @QueryParam(value = "key") String tailrKey,
                                   @QueryParam(value = "graph") String graphName,
                                   @DefaultValue("2") @QueryParam(value = "level") int level,
                                   @DefaultValue("ttl") @QueryParam(value = "lang") String lang) {
        // handle failure cases
        Lang format = RDFLanguages.nameToLang(lang);
        if (fileBytes == null || fileBytes.length == 0) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else if ("".equals(tailrKey) || format == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (!isRdfFile(new ByteArrayInputStream(fileBytes), format)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        System.out.println("got " + tailrKey);
        System.out.println("got " + graphName);
        System.out.println("got " + level);
        System.out.println("got " + format);
        return Response.ok(fileBytes, MediaType.APPLICATION_OCTET_STREAM).build();

    }

    /* check if a inputstream is valid rdf */
    private boolean isRdfFile(InputStream stream, Lang format) {
        try {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, stream, format);
        } catch (RiotException e) {
            return false;
        }
        return true;
    }
}
