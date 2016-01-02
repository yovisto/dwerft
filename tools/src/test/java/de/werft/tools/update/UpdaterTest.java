package de.werft.tools.update;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.DwerftConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Class under test {@link Updater}.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdaterTest {


    @Test
    public void testRemoteModelLoading() {
        Updater updater = Updater.createUpdater("http://dbpedia.org/sparql");
        assertTrue(updater.testConnection());
    }

    @Test
    public void testRemoteAuthenticationModelLoading() {
        DwerftConfig config = ConfigFactory.create(DwerftConfig.class);
        String serviceUrl = config.getRemoteUrl();
        HttpAuthenticator authenticator = new SimpleAuthenticator(config.getRemoteUser(), config.getRemotePass().toCharArray());
        Updater updater = Updater.createUpdater(serviceUrl, authenticator);
        assertTrue(updater.testConnection());
    }

    @Test
    public void testFileModelLoading() {
        Model model = RDFDataMgr.loadModel("src/test/resources/generic_example_cast.ttl");
        Updater updater = Updater.createUpdater(model);
        assertTrue(updater.testConnection());
    }
}