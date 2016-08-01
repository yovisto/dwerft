package de.werft.update;

import de.hpi.rdf.tailrapi.Delta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class test the different created update queries
 * that are used by the {@link Uploader}<br/>
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdateTest {

    private Delta d = new Delta();

    private static String graph = "http://example.com/g1";

    @Before
    public void setUp() {
        d.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");
    }


    @Test
    public void testRemoveQuery() throws Exception {
        Update u = new Update(Update.Granularity.LEVEL_0, d);
        String expected = "DELETE DATA { GRAPH <http://example.com/g1> { " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . } }";
        assertEquals(expected, u.convertToQuery(graph));
    }

    @Test
    public void testInsertQuery() throws Exception {
        Update u = new Update(Update.Granularity.LEVEL_1, d);
        String expected = "INSERT DATA { GRAPH <http://example.com/g1> { " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . " +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . } }";
        assertEquals(expected, u.convertToQuery(graph));
    }

    @Test
    public void testDiffQuery() throws Exception {
        Update u = new Update(Update.Granularity.LEVEL_2, d);
        String[] result = u.convertToDiffQuery("");
        Assert.assertEquals("Insert data {" +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . }", result[1]);
        Assert.assertEquals("Delete data {" +
                "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }", result[0]);

    }
}