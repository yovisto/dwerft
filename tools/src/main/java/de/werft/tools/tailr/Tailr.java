package de.werft.tools.tailr;

/**
 * The Tailr class pushes RDF files
 * to the tailr versioning system and reads
 * actual states and deltas from the tailr
 * system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Tailr {

    private String token = "";


    public Tailr(String token) {
        this.token = token;
    }
}
