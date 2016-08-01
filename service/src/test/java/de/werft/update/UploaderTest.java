package de.werft.update;

import de.hpi.rdf.tailrapi.Delta;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.*;

import java.io.File;
import java.io.IOException;

/**
 * Test the uploader with a local running fuseki.
 * Fuseki is located at /src/test/resources/fuseki
 * not provided.
 *
 * Download manually from https://jena.apache.org/download/index.cgi
 *
 * Automatic assertions are difficult, so verify the output by hand.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UploaderTest {

    private static Process fuseki;

    private static Uploader uploader;

    private static String endpoint = "http://localhost:3030/ds";

    private String graphName = "http://example.org";

    private Model expectedModel = RDFDataMgr.loadModel("src/test/resources/test.ttl");

    @BeforeClass
    public static void setUpBefore() throws IOException, InterruptedException {
        fuseki = Runtime.getRuntime().exec("./fuseki-server --mem --localhost --update /ds", null, new File("src/test/resources/fuseki"));
        uploader = new Uploader(endpoint + "/update");
        Thread.sleep(6000); // wait for fuseki to be ready
    }

    @AfterClass
    public static void tearDownAfter() throws InterruptedException {
        fuseki.destroy();
    }

    @Before
    public void setUp() {
        uploader.createGraph(graphName);
    }

    @After
    public void tearDown() {
        uploader.deleteGraph(graphName);
    }

    @Test
    public void testUpload() throws InterruptedException {
        /* test the basic upload mechanism */
        Delta d = convertModelToDelta(expectedModel);
        Update u = new Update(Update.Granularity.LEVEL_1, d);
        uploader.uploadModel(u, graphName);
        Model remoteModel = getRemoteModel(endpoint);

        Assert.assertTrue("Some failure in the isomorphism function", remoteModel.equals(expectedModel));
    }

    @Test
    public void testRemove() {
        prepareEndpoint(convertModelToDelta(expectedModel));
        Delta d = new Delta();
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded\" .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/984745> <http://filmontology.org/ontology/1.0/identifier> \"984745^^http://www.w3.org/2001/XMLSchema#int\" .");
        Update u = new Update(Update.Granularity.LEVEL_0, d);
        uploader.uploadModel(u, graphName);
        Model remote = getRemoteModel(endpoint);

        RDFDataMgr.write(System.out, remote, Lang.NT);
    }

    @Test
    public void testDiff() {
        prepareEndpoint(convertModelToDelta(expectedModel));
        Delta d = new Delta();
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded\" .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/984745> <http://filmontology.org/ontology/1.0/identifier> \"984745^^http://www.w3.org/2001/XMLSchema#int\" .");
        d.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded II^^http://www.w3.org/2001/XMLSchema#string\" .");
        Update u = new Update(Update.Granularity.LEVEL_2, d);
        uploader.uploadModel(u, graphName);
        Model remote = getRemoteModel(endpoint);

        RDFDataMgr.write(System.out, remote, Lang.NT);
    }

    /* upload data to a triple store */
    private void prepareEndpoint(Delta d) {
        Update u = new Update(Update.Granularity.LEVEL_1, d);
        uploader.uploadModel(u, graphName);
    }

    /* returns a model from a remote endpoint containing all triples */
    private Model getRemoteModel(String endpoint) {
        String queryString = "SELECT ?subject ?predicate ?object\n" +
                "FROM <" + graphName + "> WHERE { ?subject ?predicate ?object }";
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

    private Delta convertModelToDelta(Model m) {
        Delta d = new Delta();
        StmtIterator itr = m.listStatements();

        while (itr.hasNext()) {
            StringBuilder builder = new StringBuilder();
            Statement s = itr.nextStatement();
            builder.append("<").append(s.getSubject().toString()).append("> ");
            builder.append(" <").append(s.getPredicate().toString()).append("> ");

            if (s.getObject() instanceof Resource) {
                builder.append("<").append(s.getObject().toString()).append("> .");
            } else {
                builder.append("\"").append(s.getObject().asLiteral()).append("\" .");
            }
            d.getAddedTriples().add(builder.toString());
        }

        return d;
    }
}
