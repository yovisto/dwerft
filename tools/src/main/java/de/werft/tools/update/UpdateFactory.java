package de.werft.tools.update;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateException;

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

    public static Update createUpdate(Update.Granularity g, Model local, Model remote) throws UpdateException {
        if (Update.Granularity.LEVEL_0.equals(g) || Update.Granularity.LEVEL_1.equals(g)) {
            throw new UpdateException("Method could not applied on two models.");
        }
        return new Update(g, local, remote);
    }
}
