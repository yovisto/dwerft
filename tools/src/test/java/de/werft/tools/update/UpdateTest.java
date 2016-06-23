package de.werft.tools.update;

import de.hpi.rdf.tailrapi.Delta;
import de.werft.tools.general.AbstractTest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateException;
import org.junit.Assert;
import org.junit.Test;

import static de.werft.tools.update.UpdateFactory.createUpdate;
import static org.junit.Assert.assertEquals;

/**
 * This class test the different created update queries
 * that are used by the {@link Uploader}<br/>
 *
 * For the later tests run a local Fuseki instance and upload
 * the generic_example_cast_changed.ttl<br/>
 *
 * Download Fuseki from Jena and run with:<br/>
 * fuseki-server --mem --localhost --update /ds
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdateTest extends AbstractTest {

    private Model localModel = RDFDataMgr.loadModel(verificationFolder + "generic_example.ttl");

    private Model remoteModel = RDFDataMgr.loadModel(verificationFolder + "generic_example_changed.ttl");

    private static String graph = "http://example.com/g1";

    //@Before
    public void setUp() {
        Model m = RDFDataMgr.loadModel("src/test/resources/generic_example_cast_changed.ttl");
        Update u = createUpdate(Update.Granularity.LEVEL_1, m);
        Uploader uploader = new Uploader("http://localhost:3030/ds/update");
        uploader.uploadModel(u, graph);
    }

    @Test(expected = UpdateException.class)
    public void testWrongInitialization() {
        createUpdate(Update.Granularity.LEVEL_2, localModel);
    }


    @Test(expected = UpdateException.class)
    public void testWrongInitializationDiff() {
        createUpdate(Update.Granularity.LEVEL_0, new Delta());
    }


    @Test
    public void testRemoveQuery() throws Exception {
        Update u = createUpdate(Update.Granularity.LEVEL_0, localModel);
        String expected = "DELETE DATA { GRAPH <http://example.com/g1> { " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Project> . } }";
        assertEquals(expected, u.convertToQuery(graph));
    }

    @Test
    public void testInsertQuery() throws Exception {
        Update u = createUpdate(Update.Granularity.LEVEL_1, localModel);
        String expected = "INSERT DATA { GRAPH <http://example.com/g1> { " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Project> . } }";
        assertEquals(expected, u.convertToQuery(graph));
    }

    @Test
    public void testDiffQuery() throws Exception {
        Delta d = new Delta();
        d.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");

        Update u = createUpdate(Update.Granularity.LEVEL_2, d);
        String[] result = u.convertToDiffQuery("");
        Assert.assertEquals("Insert data {" +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . }", result[1]);
        Assert.assertEquals("Delete data {" +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }", result[0]);

    }
}