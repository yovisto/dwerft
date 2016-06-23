package de.werft.tools.update;

import de.hpi.rdf.tailrapi.Delta;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import static org.apache.jena.vocabulary.DB.graphName;

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

    private Delta d;

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


    public Update(Granularity g, Delta d) {
        this.g = g;
        this.d = d;
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
        StringBuilder query = new StringBuilder();

        if ("".equals(graphName)) {
            query.append(g.operation).append(" ");
        } else {
            query.append(g.operation).append(" { GRAPH <").append(graphName).append("> { ");
        }
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
    public String[] convertToDiffQuery(String graphName) throws UnsupportedOperationException {
        if ("".equals(graphName)) {
            return new String[] {d.getDeleteQuery(), d.getInsertQuery()};
        } else {
            return new String[] {d.getDeleteQuery(graphName), d.getInsertQuery(graphName)};
        }
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
