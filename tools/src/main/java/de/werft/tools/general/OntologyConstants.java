package de.werft.tools.general;

import java.io.*;

/**
 * Collected constants for the dwerft ontology
 * 
 * @author hagt
 *
 */
public class OntologyConstants {



	/*
	 * Path to the ontology file
	 */
	public static InputStream ONTOLOGY_FILE = null;

	/*
	 * Format of the ontology file
	 */
	public static final String ONTOLOGY_FORMAT = "RDF/XML";
	
	/*
	 * Namespace prefix for the ontology model
	 */
	public static final String ONTOLOGY_PREFIX = "foo";

	/*
	 * Namespace for the ontology model
	 */
	public static final String ONTOLOGY_NAMESPACE = "http://filmontology.org/ontology/2.0/";

	/*
	 * Namespace prefix for resources
	 */
	public static final String RESOURCE_PREFIX = "for";

	/*
	 * Namespace for resources
	 */
	public static final String RESOURCE_NAMESPACE = "http://filmontology.org/resource/";

	
	/*
	 * URL of the sparql endpoint
	 */
	public static final String SPARQL_ENDPOINT = "http://sparql.filmontology.org/";

	/*
	 * URI Prefixes for sparql queries
	 */
	public static final String ONTOLOGY_PREFIXES = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
			"PREFIX " + ONTOLOGY_PREFIX + ": <" + ONTOLOGY_NAMESPACE + ">" +
			"PREFIX " + RESOURCE_PREFIX + ": <" + RESOURCE_NAMESPACE + "> ";


    /**
     * Sets the ontology file from classpath. This step is absolutly necessary
     * and must be done before you use the constants.
     *
     * @param ontologyFile - path to ontology file.
     */
    public static void setOntologyFile(File ontologyFile) {
        try {
            ONTOLOGY_FILE = new BufferedInputStream(new FileInputStream(ontologyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
