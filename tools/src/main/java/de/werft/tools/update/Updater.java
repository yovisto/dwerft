package de.werft.tools.update;

/**
 *
 * An Updater is used to "update" data on an SPARQL endpoint.
 * It takes an rdf model an deletes from the root resources all nodes
 * in the subgraph in the remote model. the new subgraph is then inserted.
 * Configure your endpoint to allow updates via SPARQL!
 *
 * TODO there should be a memento system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Updater {

    private String remoteService;

    private Updater(String remoteService) {
        this.remoteService = remoteService;
    }


    public static Updater createUpdater(String remoteService) {
        return new Updater(remoteService);
    }
}
