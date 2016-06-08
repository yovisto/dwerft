package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.AbstractTest;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Live test of the tailr versioning system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class TailrTest extends AbstractTest {

    private Tailr tailr;

    private String graphName = "http://example.org";

    @Before
    public void setUp() {
        tailr = new Tailr(conf.getTailrToken(), conf.getTailrRepo());
    }

    @Test
    public void testPush() {
        Model m = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        assertTrue(tailr.addRevision(m, graphName));
    }

    @Test
    public void testGetList() throws IOException {
        List<String> result = tailr.getListOfRevisions(graphName); // verification by hand
        System.out.println(result);
    }
}