package de.werft;

import de.werft.update.Update;
import de.werft.update.Uploader;
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
    public void uploadModel(Update u, String graphUri, HttpAuthenticator auth) {
        /* noop */
    }
}
