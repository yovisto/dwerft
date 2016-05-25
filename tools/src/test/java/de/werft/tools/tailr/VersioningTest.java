package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.DwerftConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ratzeputz on 25.05.16.
 */
public class VersioningTest {


    private Versioning v;

    @Before
    public void setUp() {
        DwerftConfig conf = ConfigFactory.create(DwerftConfig.class);
        v = new Versioning(conf.getTailrToken());
    }

    @Test
    public void testPush() {
        Model m = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        

    }
}