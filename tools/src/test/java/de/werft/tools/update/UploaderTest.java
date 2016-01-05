package de.werft.tools.update;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Test the upload via a local Model.
 *
 * Run a fuseki server on localhost:3030.
 * Cmd example: fuseki-server --mem --localhost --update /ds
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UploaderTest {

    private String graphUri = "http://example.com/g1";

    @Test
    public void testUploadToModel() throws IOException {
        String endpoint ="http://localhost:3030/ds";
        Model expectedModel = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        Uploader updater = new Uploader(endpoint);

        updater.uploadModel(expectedModel, graphUri);
        updater.uploadModel("src/test/resources/generic_example_cast.ttl", graphUri);
        Model remoteModel = getRemoteModel(endpoint);
        assertTrue("Models are not isomorph.", expectedModel.isIsomorphicWith(remoteModel));
    }

    @Test
    public void testFilmontologyUpload() {
        String endpoint = "http://sparql.filmontology.org";
        Model expectedModel = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        Uploader uploader = new Uploader(endpoint);
        HttpAuthenticator auth = new SimpleAuthenticator("", "".toCharArray());

        uploader.uploadModel(expectedModel, graphUri, auth);
        Model remoteModel = getRemoteModel(endpoint);
        assertTrue("Models are not isomorph.", expectedModel.isIsomorphicWith(remoteModel));
    }

    // returns a model from a remote endpoint containing all triples
    private Model getRemoteModel(String endpoint) {
        String queryString = "SELECT ?subject ?predicate ?object\n" +
                "FROM <" + graphUri + "> WHERE { ?subject ?predicate ?object }";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint + "/sparql", query);
        ResultSet result = qexec.execSelect();

        Model m = ModelFactory.createDefaultModel();
        while (result.hasNext()) {
            QuerySolution sol = result.nextSolution();
            Resource subject = sol.getResource("?subject");
            RDFNode predicate = sol.get("?predicate");
            RDFNode object = sol.get("?object");
            m.add(subject, m.getProperty(predicate.asResource().getURI()), object);
        }
        qexec.close();
        return m;
    }
}