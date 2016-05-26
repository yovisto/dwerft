package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;

import java.util.Date;

/**
 * The Tailr class pushes RDF files
 * to the tailr versioning system and reads
 * actual states and deltas from the tailr
 * system.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Tailr {

    private String token = "";

    private String repoUrl = "";

    /**
     * Instantiates a new connector to the Tailr system.
     *
     * @param token   the token
     * @param repoUrl the repo url
     */
    public Tailr(String token, String repoUrl) {
        this.repoUrl = repoUrl;
        this.token = token;
    }

    /**
     * Add a new revision from a model.
     *
     * @param m the model for revision
     * @return the delta to the last revision
     */
    public Model addRevision(Model m) {

        return null;
    }

    /**
     * Gets an old revision from a specific date.
     *
     * @param d the revision date
     * @return the revision
     */
    public Model getRevision(Date d) {

        return null;
    }
}
