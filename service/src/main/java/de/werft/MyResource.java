package de.werft;

import de.hpi.rdf.tailrapi.Delta;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.Tailr;
import de.werft.update.Update;
import de.werft.update.Uploader;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Root resource exposed at "/upload".
 * This class handles the file uploads as a restful
 * service. All API calls are documented via swagger.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Path("/upload")
public class MyResource {

    private final static Logger L = org.apache.logging.log4j.LogManager.getLogger("UploadService.class");

    @Inject
    ServiceConfig conf;

    @Inject
    Tailr tailrClient;

    @Inject
    Uploader uploader;

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(byte[] fileBytes,
                                   @QueryParam(value = "key") String tailrKey,
                                   @QueryParam(value = "graph") String graphName,
                                   @DefaultValue("2") @QueryParam(value = "level") int level,
                                   @DefaultValue("ttl") @QueryParam(value = "lang") String lang) {
        // handle failure cases
        Lang format = RDFLanguages.nameToLang(lang);
        Update.Granularity g = null;
        try {
            g = Update.Granularity.valueOf("LEVEL_" + level);
        } catch (IllegalArgumentException e) {
            L.error("Illegal granularity level.", e);
        }

        if (fileBytes == null || fileBytes.length == 0) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else if ("".equals(tailrKey) || format == null || g == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (!isRdfFile(new ByteArrayInputStream(fileBytes), format)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        /* create new memento and retrieve delta from tailr anyway */
        Delta d = getDelta(fileBytes, tailrKey);
        if (d == null) {
            return Response.status(Response.Status.NOT_MODIFIED).build();
        }

        if ("".equals(graphName)) {
            graphName = "http://filmontology.org";
        }

        /* create authentication */
        HttpAuthenticator auth = new SimpleAuthenticator(conf.getRemoteUser(), conf.getRemotePass().toCharArray());
        uploader.uploadModel(new Update(g, d), graphName, auth);

        return Response.ok(fileBytes, MediaType.APPLICATION_OCTET_STREAM).build();

    }

    private Delta getDelta(byte[] fileBytes, @QueryParam(value = "key") String tailrKey) {
        try {
            Repository repo = new Repository(conf.getTailrUser(), conf.getTailrRepo());
            String input = new String(fileBytes);
            return tailrClient.putMemento(repo, tailrKey, input);
        } catch (IOException | URISyntaxException e) {
            L.error("Returning not modified.", e);
        }
        return null;
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
