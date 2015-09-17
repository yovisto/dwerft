package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.dwerft.lpdc.general.OntologyConstants;

public abstract class RdfExporter {
	
	private final String FILMONTOLOGY_BASE_URI = "http://filmontology.org/ontology/1.0/";
	
	/** The Logger. */
	private static final Logger L = Logger.getLogger(RdfExporter.class.getName());

	private Model model;
	
	private String sparqlEndpoint;
	
	private OntModel ontologyModel;
	
	/**
	 * Constructor for querying a remote SPARQL endpoint.
	 * 
	 * @param sparqlEndpointUrl 
	 * 				the URL of the SPARQL end point
	 * @param ontologyFilename 
	 * 				the file containing the model used with the RDF data
	 */
	public RdfExporter(String sparqlEndpointUrl, String ontologyFilename) {
		L.info("Exporting RDF from SPARQL endpoint " + sparqlEndpointUrl);
		this.sparqlEndpoint = sparqlEndpointUrl;
		this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.read(ontologyFilename, OntologyConstants.ONTOLOGY_FORMAT);
	}
	
	/**
	 * This constructor is for use with an RDF file
	 * 
	 * @param rdfInput 
	 * 				the file containing the data, in RDF format
	 * @throws IOException
	 */
	public RdfExporter(File rdfInput) throws IOException {	
		L.info("Exporting RDF from file " + rdfInput.getAbsolutePath());
		prepareARQ(rdfInput);
	}
	
	/**
	 * Prepares ARQ so regular SPARQL queries can be issued to the given file of RDF data
	 * 
	 * @throws IOException
	 */
	private void prepareARQ(File rdfInput) throws IOException {
		L.info("Preparing ARQ for use with an RDF File");
		
		InputStream in = new FileInputStream(rdfInput);
		model = ModelFactory.createMemModelMaker().createModel("");
		model.read(in, null, "TTL"); // null base URI, since model URIs are absolute
		in.close();
	}
	
	
	/**
	 * Gets the resources filtered by literal name and value.
	 *
	 * @param className the class name
	 * @param datatypeName the datatype property name
	 * @param value the value of the literal
	 * @return a list of resources
	 */
	public List<Resource> getResourcesFilteredByLiteral(String className, String datatypeName, String value) {
		List<Resource> result = new ArrayList<Resource>();
		
		for (Resource r : getResourcesByType(className)) {
			for (Literal l : getLinkedDataValues(r, datatypeName)) {
				if (StringUtils.equalsIgnoreCase(l.getString(), value)) {
					result.add(r);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Executes a query on the sparql endpoint and returns a result set.
	 * 
	 * @param queryString
	 * 						The sparql query string
	 * @return	The query result set
	 * 
	 */
	public ResultSet queryEndpoint(String queryString) {
		L.debug("Attempting to query remote SPARQL end point... ");
		
		Query query = QueryFactory.create(queryString);
		HttpAuthenticator authenticator = new SimpleAuthenticator("dwerft", "#dwerft".toCharArray());
        QueryExecution qExe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query , authenticator);

		return qExe.execSelect();
	}
	
	/**
	 * Retrieves a set of resources that have the specified type (ontology class name)
	 * 
	 * @param className
	 * 					Name of the ontology class
	 * @return All resources that have a RDF type relation to the ontology class
	 */
	public ArrayList<Resource> getResourcesByType(String className) {	
		L.debug("Requesting resources of type " + className);
		
		ArrayList<Resource> result = new ArrayList<Resource>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "?res rdf:type "+OntologyConstants.ONTOLOGY_PREFIX+":"+className
				+ "}";

		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getResource("res"));
		}
		
		return result;
		
	}
	
	/**
	 * Retrieves a set of resources that are linked to the start resource
	 * via the specified object property using default name space.
	 * 
	 * @param start
	 * 				Start resource (subject)
	 * @param objectPropertyName
	 * 				Name of the object property
	 * @return Linked resources
	 */
	public ArrayList<Resource> getLinkedResources(Resource start, String objectPropertyName) {
		return getLinkedResources(start, OntologyConstants.ONTOLOGY_PREFIX, objectPropertyName);
	}
	
	/**
	 * Retrieves a set of resources that are linked to the start resource
	 * via the specified object property. 
	 * 
	 * @param start
	 * 				Start resource (subject)
	 * @param prefix
	 * 				Namespace prefix of the object property
	 * 
	 * @param objectPropertyName
	 * 				Name of the object property
	 * @return Linked resources
	 */
	public ArrayList<Resource> getLinkedResources(Resource start, String prefix, String objectPropertyName) {
		L.debug("Requesting resources linked to " + start.getLocalName() + " via object property " + objectPropertyName);
		
		ArrayList<Resource> result = new ArrayList<Resource>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ prefix+":"+objectPropertyName + " "
				+ "?res"
				+ "}";

		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getResource("res"));
		}
		
		return result;
	}
	
	
	/**
	 * Retrieves a set of data values (literals) that are linked to the start
	 * resource via the specified datatype property. 
	 * 
	 * @param start
	 * 				Start resource (subject)
	 * @param datatypePropertyName
	 * 				Name of the datatype property
	 * @return
	 */
	public ArrayList<Literal> getLinkedDataValues(Resource start, String datatypePropertyName) {
		L.debug("Requesting literals linked to " + start.getLocalName() + " via datatype property " + datatypePropertyName);
		
		ArrayList<Literal> result = new ArrayList<Literal>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ OntologyConstants.ONTOLOGY_PREFIX+":"+datatypePropertyName + " "
				+ "?res"
				+ "}";

		
		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getLiteral("res"));
		}
				
		return result;
	}
	
	
	public Map<String,ArrayList<Literal>> getLinkedDataValues(Resource start, Set<Property> datatypeProperties) {
		L.debug("Requesting literals linked to " + start.getLocalName() + " via datatype properties " + datatypeProperties);
		
		Map<String,ArrayList<Literal>>  result = new HashMap<String,ArrayList<Literal>>();
		
		Set<String> propertyNames = new HashSet<String>();
		for (Property p : datatypeProperties) {
			propertyNames.add(p.getLocalName());
		}
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?prop ?lit where { "
				+ "<" + start.getURI() + "> "
				+ "?prop "
				+ "?lit"
				+ "}";

		
		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			
			RDFNode prop = sol.get("prop");
			RDFNode lit = sol.get("lit");
			
			if (lit.isLiteral() && prop.isResource()) {
				
				Resource resource = prop.asResource();
				Literal literal = lit.asLiteral();
				
				String propName = resource.getLocalName();
				
				if (propertyNames.contains(propName)) {
					ArrayList<Literal> values = result.get(propName);
					if (values == null) {
						values = new ArrayList<Literal>();
						result.put(propName, values);
					}
					values.add(literal);
				}
				
			}
		}
				
		return result;
	}
	
	
	
	/**
	 * TODO
	 * 
	 * @param start
	 * @return
	 */
	public Map<String, ArrayList<Literal>> getAllLinkedDataValues(Resource start) {
		
		Map<String, ArrayList<Literal>> result = new HashMap<String, ArrayList<Literal>>();
		
		ArrayList<Resource> linkedResources = getLinkedResources(start, "rdf", "type");
		Resource type = null;
		if (linkedResources.size() == 1) {
			type = linkedResources.get(0);
		} else {
			return null;
		}
		
		Set<Property> declaredProperties = getDeclaredProperties(type);
		
		result = getLinkedDataValues(start, declaredProperties);
		
//		for (Property property : declaredProperties) {
//			if (ontologyModel.getDatatypeProperty(property.getURI()) != null) {
//				ArrayList<Literal> linkedDataValues = getLinkedDataValues(start, property.getLocalName());
//				if (linkedDataValues.size() > 0) {
//					result.put(property.getLocalName(), linkedDataValues);
//				}
//			}
//		}
//		
		return result;
				
	}
	
	/**
	 * Retrieves the set of declared properties in the ontology model
	 * for the given ontology class.
	 * @param ontologyClass
	 * 						The ontology class to looked up
	 * @return A set of property names
	 */
	public Set<Property> getDeclaredProperties(Resource ontologyClass) {
		L.debug("Requesting properties for ontology class " + ontologyClass.getLocalName());
		
		Set<Property> result = new HashSet<Property>();
		
		OntClass ontClass = ontologyModel.getOntClass(ontologyClass.getURI());
		if (ontClass != null) {
			ExtendedIterator<OntProperty> props = ontClass.listDeclaredProperties();
			while (props.hasNext()) {
				OntProperty prop = (OntProperty) props.next();
				if (prop.getURI().startsWith(OntologyConstants.ONTOLOGY_NAMESPACE)) {
					result.add(prop);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * TODO convert to the new setup
	 * Gets all scenes of a project
	 * 
	 * @param projectID
	 * 				the ID  of the project containing the scenes.
	 * 				TODO: !! Currently NOT used, all scenes are retrieved regardless of projectID !!
	 * @return a XML String containing all scenes of an episode
	 */
	protected String getScenesAsXML(String projectID) {
		
		L.info("Requesting all scenes of project with ID " + projectID);;
		
		//Get all properties of the class scene
		ResultSet rs = ResultSetFactory.fromXML(getAllClassPropertiesAsXML("Scene"));
		
		ArrayList<String> properties = new ArrayList<String>();
		
		//Append all properties to ArrayList
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			String variable = cropOntNameSpace(qs.get("property").toString());
			
			if (variable != null)
				properties.add(variable);
		}
		
		//Get a Stringbuilder for creating the query
		StringBuilder sb = new StringBuilder();
		
		//Build SPARQL request
		sb.append("PREFIX filmontology: <" + FILMONTOLOGY_BASE_URI + "> \nSELECT ?scene");
		
		for (String p : properties) {
			
			//properties with multiple values need to be grouped for distinct results and are separated by a blank
			//for simplicity sake all properties are treated like they may contain multiple values
			//therefore all variable names in the result set have a plural s
			sb.append(" (CONCAT(GROUP_CONCAT(DISTINCT?" + p + ";SEPARATOR=' ')) as ?" + p + "s)");
		}
		
		sb.append("\nWHERE {\n?scene a filmontology:Scene . \n");
		
		for (String p : properties)
			sb.append("OPTIONAL {?scene filmontology:" + p + " ?" + p + " . }\n");
		
		sb.append("\n}\nGROUP BY ?scene");
		
		return executeQuery(sb.toString());
	}
	
	/**
	 * Extracts the value from a given query solution
	 * Returns an empty string instead of null if the resource/literal is empty
	 * 
	 * @param q 
	 * 			the QuerySolution
	 * @param nodeName 
	 * 			the name of the resource/literal
	 * @return 
	 * 			the value of the resource/literal or an empty string
	 */
	protected String getResourceOrLiteralValue(QuerySolution q, String nodeName) {
		
		String result = "";
		RDFNode node = q.get(nodeName);
		
		if (node != null) {
			if (node.isResource()) {
				Resource resource = (Resource) node;
				if (resource != null)
					result = resource.toString();
			} else if (node.isLiteral()) {
				Literal literal = (Literal) node;
				if (literal != null)
					result = literal.toString();
			}
		}
		return result;
	}
	
	/**
	 * Returns a XML String containing all properties of a class
	 * 
	 * @param OntClass, the name of the class
	 * @return a ResultSet with the names of the properties
	 */
	private String getAllClassPropertiesAsXML(String OntClass) {
		L.debug("Converting properties to XML format for class " + OntClass);
		
		String propertyQuery = "PREFIX filmontology: <" + FILMONTOLOGY_BASE_URI + "> "
				+ "SELECT DISTINCT ?property "
				+ "{?x a filmontology:" + OntClass + " . "
				+ " ?x ?property ?value . }";
		
		return executeQuery(propertyQuery);
	}
	
	/**
	 * Executes a given SPARQL Query and returns the XML result
	 * 
	 * @param query
	 * @return
	 */
	private String executeQuery(String query) {
		L.debug("Executing SPARQL query " + query);
		
		Query q = QueryFactory.create(query);

		//Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(q, model);
		ResultSet results = qe.execSelect();
		
		//convert result to xml
		String xmlOutput = ResultSetFormatter.asXMLString(results);
		
		//free up resources used running the query
		qe.close();	

		return xmlOutput;
	}
	
	/**
	 * Removes the base ontology URI from a given input String
	 * If the String does not contain said URI, null is returned
	 * 
	 * @param input
	 * @return
	 */
	private String cropOntNameSpace(String input) {
		if (input.contains(FILMONTOLOGY_BASE_URI))
			return input.replace(FILMONTOLOGY_BASE_URI, "");
		
		return null;
	}
		
	public abstract void export();
	
}
