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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.dwerft.lpdc.general.OntologyConstants;

/**
 * The Class RdfExporter.
 * 
 * Provides general helper methods for querying a SPARQL endpoint. 
 */
public abstract class RdfExporter {
		
	/** The Logger. */
	private static final Logger L = Logger.getLogger(RdfExporter.class.getName());

	/** The model. */
	private Model model;
	
	/** The sparql endpoint. */
	private String sparqlEndpoint;
	
	/** The ontology model. */
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
		
		//Initializing the underlying ontology model of the lpdc
		this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.read(ontologyFilename, OntologyConstants.ONTOLOGY_FORMAT);
	}
	
	/**
	 * This constructor is for use with a local RDF file. 
	 * It is encouraged to use a SPARQL endpoint instead of this local variant.
	 *
	 * @param rdfInput 				
	 * 			the file containing the data, in RDF format
	 * @throws IOException 
	 * 			Signals that an I/O exception has occurred.
	 */
	public RdfExporter(File rdfInput) throws IOException {	
		L.info("Exporting RDF from file " + rdfInput.getAbsolutePath());
		prepareARQ(rdfInput);
	}
	
	/**
	 * Prepares ARQ so regular SPARQL queries can be issued to the given file of RDF data.
	 *
	 * @param rdfInput 
	 * 			the file containing the data, in RDF format
	 * @throws IOException 
	 * 			Signals that an I/O exception has occurred.
	 */
	private void prepareARQ(File rdfInput) throws IOException {
		L.info("Preparing ARQ for use with an RDF File");
		
		//Initializing the underlying basic model
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
		L.debug("Requesting resources with literal " + datatypeName + " and value " + value);
		
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
	 * @see  
	 * 		SparqlExample
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
	 * Retrieves a set of resources that have the specified type (ontology class name).
	 *
	 * @param className 					
	 * 			Name of the ontology class
	 * @return 
	 * 			All resources that have a RDF type relation to the ontology class
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
	 * via the specified object property using default namespace.
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
	 * via the specified object property using a specified namespace.
	 * 
	 * @param start
	 * 				Start resource (subject)
	 * @param namespace
	 * 				Namespace prefix of the object property
	 * 
	 * @param objectPropertyName
	 * 				Name of the object property
	 * @return Linked resources
	 */
	public ArrayList<Resource> getLinkedResources(Resource start, String namespace, String objectPropertyName) {
		L.debug("Requesting resources linked to " + start.getLocalName() + " via object property " + objectPropertyName);
		
		ArrayList<Resource> result = new ArrayList<Resource>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ namespace + ":" + objectPropertyName + " "
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
	 * 			Start resource (subject)
	 * @param datatypePropertyName 				
	 * 			Name of the datatype property
	 * @return 
	 * 			the linked data values
	 */
	public ArrayList<Literal> getLinkedDataValues(Resource start, String datatypePropertyName) {
		L.debug("Requesting literals linked to " + start.getLocalName() + " via datatype property " + datatypePropertyName);
		
		//The resulting list
		ArrayList<Literal> result = new ArrayList<Literal>();
		
		//The SPARQL query
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ OntologyConstants.ONTOLOGY_PREFIX + ":" + datatypePropertyName + " "
				+ "?res"
				+ "}";

		//Iterating over the result set, add only the resources in column "res"
		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getLiteral("res"));
		}
				
		return result;
	}
	
	
	/**
	 * TODO
	 * 
	 * Gets the linked data values.
	 *
	 * @param start 
	 * 			
	 * @param datatypeProperties the datatype properties
	 * @return the linked data values
	 */
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
	 * TODO.
	 *
	 * @param start the start
	 * @return the all linked data values
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

		return result;
				
	}
	
	/**
	 * Retrieves the set of declared properties in the ontology model
	 * for the given ontology class.
	 * 
	 * @param ontologyClass
	 * 			The ontology class to be looked up
	 * @return 
	 * 			A set of property names
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
	 * Extracts the value from a given query solution
	 * Returns an empty string instead of null if the resource/literal is empty.
	 *
	 * @param querySolution 			
	 * 		the QuerySolution
	 * @param nodeName 			
	 * 		the name of the resource/literal
	 * @return 			
	 * 		the value of the resource/literal or an empty string
	 */
	protected String getResourceOrLiteralValue(QuerySolution querySolution, String nodeName) {
		
		String result = "";
		RDFNode node = querySolution.get(nodeName);
		
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
	 * Abstract method to be overwritten by inheriting classes.
	 */
	public abstract void export();
	
}
