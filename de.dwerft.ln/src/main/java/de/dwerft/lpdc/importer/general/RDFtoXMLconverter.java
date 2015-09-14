package de.dwerft.lpdc.importer.general;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RDFtoXMLconverter {

	protected OntologyConnector ontConn;
	protected XMLProcessor xmlProc;
	protected Mapper mapper;
	protected RdfProcessor rdfProc;
	protected TripleStoreConnector tripConn;
	
	public RDFtoXMLconverter(String ontologyFileName, String ontologyFormat, String mappingsFilename, String sparqlEndpointUrl) {
		ontConn = new OntologyConnector(ontologyFileName, ontologyFormat);
		mapper = new Mapper(mappingsFilename);
		rdfProc = new RdfProcessor(ontConn);
		tripConn = new TripleStoreConnector(ontConn, sparqlEndpointUrl);
	}
	
//	public Map<String, String> getAvailableProjects() {
//		Map<String, String> result = new HashMap<String, String>();
//		
//		ArrayList<Resource> resources = tripConn.getResourcesByType(OntologyConstants.ONTOLOGY_NAMESPACE+"Project");
//
//		for (Resource resource : resources) {
//			resource.get
//			
//			
//		}
//		
//		return result;
//	}
//	
	
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
			
			if (targetOntologyProperty != null && !"".equals(targetOntologyProperty)) {
				properties.add(targetOntologyProperty);
			}
		}
		
//		Set<String> keySet = mappedOntologyElements.keySet();
		
		
		
		for (String key : mappedOntologyElements.keySet()) {
			Set<String> props = mappedOntologyElements.get(key);
			
			System.out.println(key);
			for (String string : props) {
				System.out.println("-- "+string);
			}
			
		}
	
	}

}
