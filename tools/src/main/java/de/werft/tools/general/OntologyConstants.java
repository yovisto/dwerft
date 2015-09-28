package de.werft.tools.general;

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
	public static final String ONTOLOGY_FILE = "file:ontology/dwerft-ontology.owl";
	
	
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
	public static final String ONTOLOGY_NAMESPACE = "http://filmontology.org/ontology/1.0/";

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

	public static final String SPARQL_ENDPOINT_USER = "dwerft";

	public static final String SPARQL_ENDPOINT_PASSWORD = "#dwerft";
	
	/*
	 * URI Prefixes for sparql queries
	 */
	public static final String ONTOLOGY_PREFIXES = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
			"PREFIX "+ONTOLOGY_PREFIX+": <"+ONTOLOGY_NAMESPACE+">" +
			"PREFIX "+RESOURCE_PREFIX+": <"+RESOURCE_NAMESPACE+"> ";


}
