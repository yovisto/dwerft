package de.werft;


import de.hpi.rdf.tailrapi.Delta;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.auth.HttpAuthenticator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Basic upload tool which takes an URI and simply uploads a model
 * to the SPARQL endpoint.
 * Be careful when uploading anything without knowing the remote graph.
 * This can lead to data inconsistency due to merging done by sparql.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Uploader {

    private final static Logger L = org.apache.logging.log4j.LogManager.getLogger("UploadService.class");

    private String endpoint;

    /**
     * Instantiates a new Uploader.
     *
     * @param endpoint the SPARQL endpoint
     */
    public Uploader(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Uploads a model.
     *
     * @param u        the update
     * @param graphUri the graph uri
     */
    public void uploadModel(Delta u, String graphUri, HttpClient client) {
        //SparqlService service = FluentSparqlService.http(endpoint, graphUri, client).create();
        //String s = u.toSparql(graphUri);
        List<String> queries = paginate(u,graphUri);

        for (String query : queries) {
            SparqlService service = FluentSparqlService.http(endpoint, graphUri, client).create();
            L.info("SPARQL Update\n" + query);
            UpdateRequest request = UpdateRequestUtils.parse(query);
            service.getUpdateExecutionFactory()
                    .createUpdateProcessor(request)
                    .execute();
        }
    }

    /**
     * Uploads a model.
     *
     * @param u        the update
     * @param graphUri the graph uri
     */
    public void uploadModel(Delta u, String graphUri) {
        SparqlService service = FluentSparqlService.http(endpoint, graphUri).create();
        List<String> queries = paginate(u, graphUri);

        //System.out.println(queries.size());
        for (String query : queries) {
            L.info("SPARQL Update\n" + query);
            service.getUpdateExecutionFactory().createUpdateProcessor(query).execute();
        }
        return;
    }

    /**
     * Create graph.
     * Internal only
     *
     * @param graphName the graph name
     * @param auth      the auth
     */
    void createGraph(String graphName, HttpAuthenticator... auth) {
        String query = "CREATE GRAPH <" + graphName + ">";
        UpdateRequest request = UpdateFactory.create();
        request.add(query);
        update(request, auth);
    }

    /**
     * Delete graph.
     * Internal only
     *
     * @param graphName the graph name
     * @param auth      the auth
     */
    void deleteGraph(String graphName, HttpAuthenticator... auth) {
        String query = "DROP SILENT GRAPH <" + graphName + ">";
        UpdateRequest request = UpdateFactory.create();
        request.add(query);
        update(request, auth);
    }

    private List<String> paginate(Delta d, String graphUri) {
        List<String> queries = new ArrayList<>();

        if (d.getRemovedTriples().size() > 0) {
            int chunks = d.getRemovedTriples().size() / 500;
            for (int i = 0; i <= chunks; i++) {
                StringBuilder builder = new StringBuilder();
                builder.append("Delete { Graph <").append(graphUri).append("> {\n");
                ArrayList<String> whereClause = new ArrayList<>();

                List<String> chunk = new ArrayList<>();
                for (int j = i * 500; j < (i + 1) * 500 && j < d.getRemovedTriples().size(); j++) {
                    chunk.add(d.getRemovedTriples().get(j));
                }

                //int end = d.getRemovedTriples().size() < i * 500 ? d.getRemovedTriples().size() - i * 500  : 500;
                //List<String> chunk = d.getRemovedTriples().subList(i, end);


                for (String triple : handleBlankNodes(chunk)) {
                    builder.append(triple.replace("_:", "?")).append(" ");
                    if (triple.contains("?")) whereClause.add(triple);
                }
                builder.append("} }\n");

                /* handle using <> where clause */
                //if (!whereClause.isEmpty())
                builder.append("Where {\n");
                for (String blankNode : whereClause) {
                    builder.append(blankNode).append(" ");
                }
                //if (!whereClause.isEmpty())
                builder.append("}\n");
                queries.add(builder.toString());
            }
        }

        if (d.getAddedTriples().size() > 0) {
            int chunks = d.getAddedTriples().size() / 500;
            for (int i = 0; i <= chunks; i++) {
                StringBuilder builder = new StringBuilder();
                builder.append( "INSERT { Graph <").append(graphUri).append("> {\n");


                List<String> chunk = new ArrayList<>();
                for (int j = i * 500; j < (i + 1) * 500 && j < d.getAddedTriples().size(); j++) {
                    chunk.add(d.getAddedTriples().get(j));
                }
                //int end = d.getRemovedTriples().size() < i * 500 ? d.getAddedTriples().size() - i * 500  : 500;
                //List<String> chunk = d.getRemovedTriples().subList(i, end);

                for (String triple : chunk) {
                    builder.append(triple.replace("  ", " ")).append(" ");
                }

                builder.append("\n} }Where {\n}\n");
                queries.add(builder.toString());
            }
        }

        return queries;
    }

    static List<String> handleBlankNodes(List<String> triples) {
        ArrayList<String> handledTriples = new ArrayList<>();
        Iterator<String> itr = triples.iterator();

        while(itr.hasNext()) {
            String triple = itr.next();
            handledTriples.add(triple.replace("_:", "?"));
        }

        return handledTriples;
    }

    private void update(UpdateRequest request, HttpAuthenticator... auth) {
        if (auth.length == 1) {
         //   UpdateProcessor update = UpdateExecutionFactory.createRemote(request, endpoint, auth[0]);
         //   update.execute();
        } else {
            UpdateProcessor update = UpdateExecutionFactory.createRemote(request, endpoint);
            update.execute();
        }
    }
}
