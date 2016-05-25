package de.werft.tools.tailr;

/**
 * The Versioning class pushes RDF files
 * to the tailr versioning system and reads
 * actual states and deltas from the tailr
 * system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Versioning {

    private String token = "";


    public Versioning(String token) {
        this.token = token;
    }
}
