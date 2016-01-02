package de.werft.tools.update;

import org.junit.Test;

/**
 * Class under test {@link Updater}.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdaterTest {


    @Test
    public void testRemoteModelLoading() {
        String serviceUrl = "sparql.filmontology.org";
        Updater updater = Updater.createUpdater(serviceUrl);

    }
}