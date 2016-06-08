package de.werft.tools.update;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * Basic upload tool which takes an URI and simply uploads a model
 * to the SPARQL endpoint.
 * Be careful when uploading anything without knowing the remote graph.
 * This can lead to data inconsistency due to merging done by sparql.
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
     * Uploads a model.
     *
     * @param u        the update
     * @param graphUri the graph uri
     * @param auth     the auth
     */
    public void uploadModel(Update u, String graphUri, HttpAuthenticator auth) {
        if (u.getGranularity().equals(Update.Granularity.LEVEL_0) ||
                u.getGranularity().equals(Update.Granularity.LEVEL_1)) {
            UpdateRequest request = UpdateFactory.create();
            request.add(u.convertToQuery(graphUri));
            update(request, auth);
        } else {
            //TODO upload diff
        }
    }

    /**
     * Uploads a model.
     *
     * @param u        the update
     * @param graphUri the graph uri
     */
    public void uploadModel(Update u, String graphUri) {
        if (u.getGranularity().equals(Update.Granularity.LEVEL_0) ||
                u.getGranularity().equals(Update.Granularity.LEVEL_1)) {
            UpdateRequest request = UpdateFactory.create();
            request.add(u.convertToQuery(graphUri));
            update(request);
        } else {
            //TODO upload diff
        }
    }

    /**
     * Create graph.
     * Internal only
     *
     * @param graphName the graph name
     * @param auth      the auth
     */
    protected void createGraph(String graphName, HttpAuthenticator... auth) {
        String query = "CREATE GRAPH <" + graphName + ">";
        UpdateRequest request = UpdateFactory.create();
        request.add(query);

    }

    /**
     * Delete graph.
     * Internal only
     *
     * @param graphName the graph name
     * @param auth      the auth
     */
    protected void deleteGraph(String graphName, HttpAuthenticator... auth) {
        String query = "DROP SILENT GRAPH <" + graphName + ">";
        UpdateRequest request = UpdateFactory.create();
        request.add(query);
        update(request, auth);
    }

    private void update(UpdateRequest request, HttpAuthenticator... auth) {
        if (auth.length == 1) {
            UpdateProcessor update = UpdateExecutionFactory.createRemote(request, endpoint, auth[0]);
            update.execute();
        } else {
            UpdateProcessor update = UpdateExecutionFactory.createRemote(request, endpoint);
            update.execute();
        }
    }
}
