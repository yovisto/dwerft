package de.werft.tools.update;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.RDFDataMgr;

/**
 * Basic upload tool which takes an URI and simply uploads a model
 * to the SPARQL endpoint.
 * Be careful when uploading anything without knowing the remote graph.
 * This can lead to data inconsistency.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Uploader {

    private String endpoint;

    /**
     * Instantiates a new Uploader.
     *
     * @param endpoint the SPARQL endpoint
     */
    public Uploader(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Upload a model to a SPARQL endpoint.
     *
     * @param m        the update model
     * @param graphUri the graph uri the model is inserted into
     */
    public void uploadModel(Model m, String graphUri) {
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(endpoint);
        accessor.add(graphUri, m);
    }

    /**
     * Upload a model to a restricted SPARQL endpoint.
     *
     * @param m        the update model
     * @param graphUri the graph uri the model is inserted into
     * @param auth     the credentials
     */
    public void uploadModel(Model m, String graphUri, HttpAuthenticator auth) {
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(endpoint, auth);
        accessor.add(graphUri, m);
    }

    /**
     * Upload a file to a restricted SPARQL endpoint.
     *
     * @param location the file location of the rdf file
     * @param graphUri the graph uri the model is inserted into
     * @param auth     the credentials
     */
    public void uploadModel(String location, String graphUri, HttpAuthenticator auth) {
        uploadModel(getModel(location), graphUri, auth);
    }

    /**
     * Upload a file to a SPARQL endpoint.
     *
     * @param location the file location of the rdf file
     * @param graphUri the graph uri the model is inserted into
     */
    public void uploadModel(String location, String graphUri) {
        uploadModel(getModel(location), graphUri);
    }


    private Model getModel(String location) {
        return RDFDataMgr.loadModel(location);
    }
}
