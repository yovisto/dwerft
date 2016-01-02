package de.werft.tools.update;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

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

    private static final Logger LOGGER = LogManager.getLogger(Updater.class);

    /**
     * Create queryexecution query execution.
     *
     * @param query the query
     * @return the query execution
     */
    protected abstract QueryExecution createQueryexecution(Query query);

    protected abstract GraphStore createGraphStore();

        /**
     * Test connection returns boolean and prints console output.
     *
     * @return the boolean
     */
    protected boolean testConnection() {
        Query q = QueryFactory.create("select distinct ?Concept where {[] a ?Concept} LIMIT 10");
        QueryExecution quexec = createQueryexecution(q);
        ResultSet set = quexec.execSelect();
        ResultSetFormatter.out(System.out, set);
        quexec.close();
        return true;
    }


    public void updateModel(Model m, String rootResource) {
        RDFNode root = getRootNode(m, rootResource);
        if (root == null) {
            LOGGER.error("A model needs a root resources");
            return;
        }

        // fetch remote resource
        Model remoteModel = ModelFactory.createDefaultModel();
        getRemoteResources(root.asResource(), remoteModel);
        remoteModel.write(System.out, "TTL");

        StmtIterator statements = m.listStatements();
        List<Statement> delete = new ArrayList<>();
        List<Statement> update = new ArrayList<>();

        while (statements.hasNext()) {
            Statement stmt = statements.nextStatement();
            Resource newSubject = stmt.getResource();
            // resource remains the same
            if (m.containsResource(newSubject)) {
                // we have an unequal statement, this should be updated
                if (!m.contains(stmt)) {
                    update.add(stmt);
                }
            }
        }

    }

    // query remote model
    private void getRemoteResources(Resource localRoot, Model m) {
        Query query = QueryFactory.create("select distinct ?p ?o where {<" + localRoot.getURI() + "> ?p ?o . }");
        QueryExecution quexec = createQueryexecution(query);
        ResultSet set = quexec.execSelect();

        // query corresponding remote resource; take last one
        while (set.hasNext()) {
            QuerySolution sol = set.nextSolution();
            //System.out.println(sol);

            Property prop = m.createProperty(sol.getResource("?p").getURI());
            RDFNode data = sol.get("?o");

            // we have another resource; dig deeper
            if (!data.isLiteral() && !prop.getURI().contains("rdf-syntax-ns#type")) {
                getRemoteResources(data.asResource(), m);
            }
            Statement s = new StatementImpl(localRoot, prop, data);
            m.add(s);
        }
        quexec.close();
    }

    // get root node
    private RDFNode getRootNode(Model m, String rootResource) {
        Resource root = null;
        ResIterator itr = m.listSubjects();
        while (itr.hasNext() && root == null) {
            Resource res = itr.next();
            if (res.getURI().contains(rootResource)) {
                root = res;
            }
        }
        return root;
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

        @Override
        protected GraphStore createGraphStore() {
            return GraphStoreFactory.create(dataset);
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

        @Override
        protected GraphStore createGraphStore() {
            return GraphStoreFactory.create();
        }

    }
}
