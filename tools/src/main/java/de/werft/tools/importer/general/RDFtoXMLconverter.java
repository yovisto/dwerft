package de.werft.tools.importer.general;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * THIS CLASS IS CURRENTLY NOT IN USE
 * 
 * The Class RDFtoXMLconverter.
 */
public class RDFtoXMLconverter {

	/** The Logger. */
	private static final Logger L = Logger.getLogger(RDFtoXMLconverter.class.getName());
	
	/** The ont conn. */
	protected OntologyConnector ontConn;
	
	/** The xml proc. */
	protected XMLProcessor xmlProc;
	
	/** The mapper. */
	protected Mapper mapper;
	
	/** The rdf proc. */
	protected RdfProcessor rdfProc;
	
	/** The trip conn. */
	protected TripleStoreConnector tripConn;
	
	/**
	 * Instantiates a new RDF to xml converter.
	 *
	 * @param ontologyFileName 
	 * 		the ontology file name
	 * @param ontologyFormat 
	 * 		the ontology format
	 * @param mappingsFilename 
	 * 		the mappings filename
	 * @param sparqlEndpointUrl 
	 * 		the sparql endpoint url
	 */
	public RDFtoXMLconverter(String ontologyFileName, String ontologyFormat, String mappingsFilename, String sparqlEndpointUrl) {
		ontConn = new OntologyConnector(ontologyFileName, ontologyFormat);
		mapper = new Mapper(mappingsFilename);
		rdfProc = new RdfProcessor(ontConn);
		tripConn = new TripleStoreConnector(ontConn, sparqlEndpointUrl);
	}
	
	/**
	 * Convert.
	 *
	 * @param projectIdentifier the project identifier
	 * @param os the os
	 */
	public void convert(String projectIdentifier, OutputStream os) {
		
		xmlProc = new XMLProcessor();
		
		Map<String,Set<String>> mappedOntologyElements = new HashMap<String, Set<String>>();

		Set<MappingDefinition> mappings = mapper.getMappings();
		
		for (MappingDefinition mappingDefinition : mappings) {
			String targetOntologyClass = mappingDefinition.getTargetOntologyClass();
			String targetOntologyProperty = mappingDefinition.getTargetOntologyProperty();
			
			Set<String> properties = mappedOntologyElements.get(targetOntologyClass);
			
			if (properties == null) {
				properties = new HashSet<String>();
				mappedOntologyElements.put(targetOntologyClass, properties);
			}
			
			if (targetOntologyProperty != null && !targetOntologyProperty.equals("")) {
				properties.add(targetOntologyProperty);
			}
		}

		//Print all keys and their mapped ontology elements
		for (String key : mappedOntologyElements.keySet()) {
			Set<String> props = mappedOntologyElements.get(key);
			
			L.debug(key);
			for (String string : props) {
				L.debug("-- " + string);
			}	
		}
	}
}
