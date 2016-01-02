package de.werft.tools.update;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 *
 * An Updater is used to "update" data on an SPARQL endpoint.
 * It takes an rdf model an deletes from the root resources all nodes
 * in the subgraph in the remote model. the new subgraph is then inserted.
 * Configure your endpoint to allow updates via SPARQL!
 *
 * TODO there should be a memento system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public abstract class Updater {

    abstract QueryExecution createQueryexecution(Query query);

    public static Updater createUpdater(String remoteService) {
        return new RemoteUpdater(remoteService, null);
    }

    public static Updater createUpdater(String remoteService, HttpAuthenticator authenticator) {
        return new RemoteUpdater(remoteService, authenticator);
    }

    public static Updater createUpdater(Model m) {
        Dataset ds = DatasetFactory.create(m);
        return new FileUpdater(ds);
    }

    public boolean testConnection() {
        String query = "select distinct ?Concept where {[] a ?Concept} LIMIT 10";

        Query q = QueryFactory.create(query);
        QueryExecution quexec = createQueryexecution(q);
        ResultSet set = quexec.execSelect();
        ResultSetFormatter.out(System.out, set);

        return true;
    }

    static class FileUpdater extends Updater {

        private Dataset dataset;

        public FileUpdater(Dataset ds) {
            this.dataset = ds;
        }

        @Override
        QueryExecution createQueryexecution(Query query) {
            return QueryExecutionFactory.create(query, dataset);
        }
    }

    static class RemoteUpdater extends Updater {

        private String serviceUrl;

        private HttpAuthenticator authenticator;

        public RemoteUpdater(String serviceUrl, HttpAuthenticator authenticator) {
            this.serviceUrl = serviceUrl;
            this.authenticator = authenticator;
        }

        @Override
        QueryExecution createQueryexecution(Query query) {
            if (authenticator != null) {
                return QueryExecutionFactory.sparqlService(serviceUrl, query, authenticator);
            } else {
                return QueryExecutionFactory.sparqlService(serviceUrl, query);
            }
        }
    }
}
