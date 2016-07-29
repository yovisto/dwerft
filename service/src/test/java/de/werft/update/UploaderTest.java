package de.werft.update;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UploaderTest {

    private static Process fuseki;

    private static Uploader uploader;

    private String graphName = "http://example.org";

    @BeforeClass
    public static void setUpBefore() throws IOException, InterruptedException {
        fuseki = Runtime.getRuntime().exec("./fuseki-server --mem --localhost --update /ds", null, new File("src/test/resources/fuseki"));
        uploader = new Uploader("http://localhost:3030/ds/update");
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
        //fuseki.waitFor();
        getRemoteModel("http://localhost:3030/ds").write(System.out);
    }

    // returns a model from a remote endpoint containing all triples
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
}
