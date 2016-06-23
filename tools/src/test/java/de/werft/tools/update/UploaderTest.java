package de.werft.tools.update;

import de.werft.tools.general.AbstractTest;
import de.werft.tools.general.DwerftConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

/**
 * Test the upload via a local Model.
 *
 * Run a fuseki server on localhost:3030.
 * Cmd example: fuseki-server --mem --localhost --update /ds
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UploaderTest extends AbstractTest {

    private String graphUri = "http://example.com/g1";

    private Model expectedModel = RDFDataMgr.loadModel(verificationFolder + "generic_example.ttl");

    @Test
    public void testInsertToLocalServer() throws IOException {
        String endpoint ="http://localhost:3030/ds";
        Uploader uploader = new Uploader(endpoint + "/update");

        Update u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_1, expectedModel);
        uploader.uploadModel(u, graphUri);
        Model remoteModel = getRemoteModel(endpoint);
        uploader.deleteGraph(graphUri);

        assertTrue("Models are not isomorph. Lists are always not isomorph.", expectedModel.isIsomorphicWith(remoteModel));
    }

    @Test
    public void testDeleteToLocalServer() throws IOException {
        String endpoint ="http://localhost:3030/ds";
        Uploader uploader = new Uploader(endpoint + "/update");

        // upload something before deleting
        Update u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_1, expectedModel);
        uploader.uploadModel(u, graphUri);
        u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_0, expectedModel);
        uploader.uploadModel(u, graphUri);
        Model remoteModel = getRemoteModel(endpoint);
        uploader.deleteGraph(graphUri);

        assertTrue("Models are not isomorph. Lists are always not isomorph.", remoteModel.isEmpty());
    }

    //@Test
    public void testFilmontologyInsert() {
        DwerftConfig cfg = ConfigFactory.create(DwerftConfig.class);
        String endpoint = "http://sparql.filmontology.org";
        Uploader uploader = new Uploader(endpoint);
        HttpAuthenticator auth = new SimpleAuthenticator(cfg.getRemoteUser(), cfg.getRemotePass().toCharArray());

        uploader.createGraph(graphUri, auth);
        Update u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_1, expectedModel);
        uploader.uploadModel(u, graphUri);
        Model remoteModel = getRemoteModel(endpoint);
        System.out.println(remoteModel); /* should be filled */

        uploader.deleteGraph(graphUri, auth);
        //assertTrue("Models are not isomorph. List are alwys not isomorph.", expectedModel.isIsomorphicWith(remoteModel));
    }

    //@Test
    public void testFilmontologyDelete() {
        DwerftConfig cfg = ConfigFactory.create(DwerftConfig.class);
        String endpoint = "http://sparql.filmontology.org";
        Uploader uploader = new Uploader(endpoint);
        HttpAuthenticator auth = new SimpleAuthenticator(cfg.getRemoteUser(), cfg.getRemotePass().toCharArray());

        uploader.createGraph(graphUri, auth);
        Update u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_1, expectedModel);
        uploader.uploadModel(u, graphUri);
        u = UpdateFactory.createUpdate(Update.Granularity.LEVEL_0, expectedModel);
        uploader.uploadModel(u, graphUri);
        Model remoteModel = getRemoteModel(endpoint);
        System.out.println(remoteModel); /* should be empty */

        uploader.deleteGraph(graphUri, auth);
        //assertTrue("Models are not isomorph. List are alwys not isomorph.", expectedModel.isIsomorphicWith(remoteModel));
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