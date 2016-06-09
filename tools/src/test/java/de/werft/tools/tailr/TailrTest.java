package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.AbstractTest;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Live test of the tailr versioning system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class TailrTest extends AbstractTest {

    private Tailr tailr;

    private String keyName = "http://example.org";

    @Before
    public void setUp() {
        this.tailr = new Tailr(conf.getTailrToken(), conf.getTailrRepo());
    }

    @Test
    public void testPush() {
        Model m = RDFDataMgr.loadModel(verificationFolder + "generic_example.ttl");
        assertTrue(tailr.addRevision(m, keyName));
    }

    @Test
    public void testGetList() {
        List<String> result = tailr.getListOfRevisions(keyName); // verification by hand
        System.out.println(result);
    }

    @Test
    public void testGetLatestRevision() {
        Model m = tailr.getLatestRevision(keyName);
        if (!m .isEmpty()) {
            RDFDataMgr.write(System.out, m, Lang.TTL);
        }
    }

    @Test
    public void testGetRevision() {
        Model m = tailr.getRevision("2016-05-27-18:12:00", keyName);
        if (!m.isEmpty()) {
            RDFDataMgr.write(System.out, m, Lang.TTL);
        }
    }

    @Test
    public void testGetNoRevision() {
        Model m = tailr.getRevision("2015-05-27-18:12:00", keyName);
        assertTrue(m.isEmpty());
    }

    @Test
    public void testDelta() {
        String result = tailr.getDelta("2016-05-27-18:12:00", keyName);
        System.out.println(result);
    }

    @Test
    public void testLatestDelta() {
        String result = tailr.getLatestDelta(keyName);
        System.out.println(result);
    }

    @Test
    public void testNoDelta() {
        String result = tailr.getDelta("2015-05-27-18:12:00", keyName);
        assertThat("", is(result));
    }

}