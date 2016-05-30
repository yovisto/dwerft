package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.DwerftConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

/**
 * Live test of the tailr versioning.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class TailrTest {

    private Tailr tailr;

    private String repoUrl = "http://tailr.s16a.org/api/santifa/dwerft?";

    private String graphName = "http://example.org";

    @Before
    public void setUp() {
        DwerftConfig conf = ConfigFactory.create(DwerftConfig.class);
        tailr = new Tailr(conf.getTailrToken(), repoUrl);
    }

    @Test
    public void testPush() {
        Model m = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        tailr.addRevision(m, graphName);
    }
}