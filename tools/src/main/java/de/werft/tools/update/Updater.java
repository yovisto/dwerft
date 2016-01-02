package de.werft.tools.update;

import com.hp.hpl.jena.query.*;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * An Updater is used to "update" data on an SPARQL endpoint.
 * It takes an rdf model an deletes from the root resources all nodes
 * in the subgraph in the remote model. the new subgraph is then inserted.
 * Configure your endpoint to allow updates via SPARQL!
 * <p>
 * TODO there should be a memento system.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public abstract class Updater {

    /**
     * Create queryexecution query execution.
     *
     * @param query the query
     * @return the query execution
     */
    protected abstract QueryExecution createQueryexecution(Query query);

        /**
     * Test connection returns boolean and prints console output.
     *
     * @return the boolean
     */
    protected boolean testConnection() {
        String query = "select distinct ?Concept where {[] a ?Concept} LIMIT 10";

        Query q = QueryFactory.create(query);
        QueryExecution quexec = createQueryexecution(q);
        ResultSet set = quexec.execSelect();
        ResultSetFormatter.out(System.out, set);

        return true;
    }

    /**
     * The type File updater.
     * This subclass manages the model access through a file.
     */
    public static class FileUpdater extends Updater {

        private Dataset dataset;

        /**
         * Instantiates a new File updater.
         *
         * @param ds the ds
         */
        public FileUpdater(Dataset ds) {
            this.dataset = ds;
        }

        @Override
        protected QueryExecution createQueryexecution(Query query) {
            return QueryExecutionFactory.create(query, dataset);
        }
    }

    /**
     * The type Remote updater.
     * This subclass manages the model access through an SPARQL endpoint
     * with out without authentication.
     */
    public static class RemoteUpdater extends Updater {

        private String serviceUrl;

        private HttpAuthenticator authenticator;

        /**
         * Instantiates a new Remote updater.
         *
         * @param serviceUrl    the service url
         * @param authenticator the authenticator
         */
        public RemoteUpdater(String serviceUrl, HttpAuthenticator authenticator) {
            this.serviceUrl = serviceUrl;
            this.authenticator = authenticator;
        }

        @Override
        protected QueryExecution createQueryexecution(Query query) {
            if (authenticator != null) {
                return QueryExecutionFactory.sparqlService(serviceUrl, query, authenticator);
            } else {
                return QueryExecutionFactory.sparqlService(serviceUrl, query);
            }
        }
    }
}
