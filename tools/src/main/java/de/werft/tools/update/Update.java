package de.werft.tools.update;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * An update is a sparql query for uploading to an sparql endpoint.
 * The update is generated given a local rdf model and a granularity
 * and an optional remote model.<br/>
 * <p>
 * <p>
 * There are three possible granularity's:<br/>
 * LEVEL_0: Create a sparql query that removes the local model
 * from the remote model.<br/>
 * LEVEL_1: Creates a simple insert query. Nothing is removed from the
 * remote Model and what will be replaced is up to sparql.
 * LEVEL_2: Creates a diff between the local and remote model. This
 * is done through exhaustive comparison and needs plenty of resources and time.
 * </p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Update {

    /**
     * The Granularity.
     */
    public enum Granularity {
        /**
         * The Level 0 deletes data.
         */
        LEVEL_0 ("DELETE DATA"),
        /**
         * The Level 1 inserts data.
         */
        LEVEL_1 ("INSERT DATA"),
        /**
         * Level 2 creates a diff.
         */
        LEVEL_2 ("DIFF");

        private String operation;

        Granularity(String s) {
            this.operation = s;
        }
    }

    private Granularity g;

    private Model local;

    private Model remote;

    /**
     * Instantiates a new insert/delete Update.
     *
     * @param g the g
     * @param m the m
     */
    public Update(Granularity g, Model m) {
        this.g = g;
        this.local = m;
    }

    /**
     * Instantiates a new diff Update.
     *
     * @param g      the g
     * @param local  the local
     * @param remote the remote
     */
    public Update(Granularity g, Model local, Model remote) {
        this.g = g;
        this.local = local;
        this.remote = remote;
    }


    /**
     * Gets the selected granularity
     *
     * @return the granularity
     */
    public Granularity getGranularity() {
        return g;
    }

    /**
     * creates a query string.
     *
     * @param graphName the graph name
     * @return the query string
     * @throws UnsupportedOperationException if you call this with granularity level2
     */
    public String convertToQuery(String graphName) throws UnsupportedOperationException {
        if (Granularity.LEVEL_2.equals(g)) {
            throw new UnsupportedOperationException("Method could not applied on diff queries.");
        }
        StringBuilder query = new StringBuilder();

        query.append(g.operation).append(" { GRAPH <").append(graphName).append("> { ");
        query.append(convertModelToQuery(local));
        return query.toString();
    }

    /**
     * creates a diff query string.
     *
     * @param gaphName the gaph name
     * @return the query string array with two elements (insert + delete)
     * @throws UnsupportedOperationException if called with granularity level 0 or 1
     */
    public String[] convertToDiffQuery(String gaphName) throws UnsupportedOperationException {
        if (Granularity.LEVEL_0.equals(g) || Granularity.LEVEL_1.equals(g)) {
            throw new UnsupportedOperationException("Method could not applied on simple queries.");
        }
        //TODO generate diff
        return new String[] {"", ""};
    }

    /* convert a all model elements to tuples */
    private String convertModelToQuery(Model m) {
        StringBuilder builder = new StringBuilder();
        StmtIterator itr = m.listStatements();

        while (itr.hasNext()) {
            Statement stmt = itr.nextStatement();
            builder.append("<").append(stmt.getSubject().getURI()).append("> ");
            builder.append("<").append(stmt.getPredicate().getURI()).append("> ");
            if (stmt.getObject().isResource()) {
                builder.append("<").append(stmt.getObject().asResource().getURI()).append("> . ");
            } else {
                builder.append("\"").append(stmt.getObject().asLiteral().getString())
                        .append("\"^^<").append(stmt.getObject().asLiteral().getDatatypeURI()).append("> . ");
            }
        }
        builder.append("} }");
        return builder.toString();
    }
}
