package de.werft.tools.update;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateException;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * Creates updates for the uploader.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UpdateFactory {


    public static Update createUpdate(Update.Granularity g, Model local) throws UpdateException {
        if (Update.Granularity.LEVEL_2.equals(g)) {
            throw new UpdateException("For a complete diff, a remote Model is needed");
        } else {
            return new Update(g, local);
        }
    }

    public static Update createUpdate(Update.Granularity g, Model local, Model remote) {
        return new Update(g, local, remote);
    }

    //TODO create diff queries
    public static Update createUpdate(Update.Granularity g, Model local, String remoteAdress,
                                      String graphName) {
        return null;
    }

    //TODO create diff queries
    public static Update createUpdate(Update.Granularity g, Model local, String remoteAdress,
                                      HttpAuthenticator auth, String graphName) {
        return null;
    }

}
