package de.werft.tools.update;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * Create Updater which updates backend graphs with provided graphs.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdaterFactory {

    /**
     * Create updater.
     *
     * @param remoteService the remote service
     * @return the updater
     */
    public static Updater createUpdater(String remoteService) {
        return new Updater.RemoteUpdater(remoteService, null);
    }

    /**
     * Create updater.
     *
     * @param remoteService the remote service
     * @param authenticator the authenticator
     * @return the updater
     */
    public static Updater createUpdater(String remoteService, HttpAuthenticator authenticator) {
        return new Updater.RemoteUpdater(remoteService, authenticator);
    }

    /**
     * Create updater.
     *
     * @param m the m
     * @return the updater
     */
    public static Updater createUpdater(Model m) {
        Dataset ds = DatasetFactory.create(m);
        return new Updater.FileUpdater(ds);
    }


}
