package de.werft;

import de.hpi.rdf.tailrapi.Delta;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * A simple stub class for testing.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UploaderStub extends Uploader {
    /**
     * Instantiates a new Uploader.
     *
     * @param endpoint the SPARQL endpoint
     */
    public UploaderStub(String endpoint) {
        super(endpoint);
    }

    @Override
    public void uploadModel(Delta u, String graphUri, HttpAuthenticator auth) {
        System.out.println(u);
        /* noop */
    }
}
