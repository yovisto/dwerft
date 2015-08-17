package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dwerft.lpdc.general.OntologyConstants;

public abstract class RdfExporter {
	
	private final String FILMONTOLOGY_BASE_URI = "http://filmontology.org/ontology/1.0/";
	
	private File rdfInput;
	private Model model;
	
	private String sparqlEndpoint;
	
	public RdfExporter(String sparqlEndpointUrl) {
		this.sparqlEndpoint = sparqlEndpointUrl;
	}
	
	public RdfExporter(File rdfInput) throws IOException {
		this.rdfInput = rdfInput;	
		prepareARQ();
	}
	
	private void prepareARQ() throws IOException {
		InputStream in = new FileInputStream(rdfInput);
		model = ModelFactory.createMemModelMaker().createModel("");
		model.read(in, null, "TTL"); // null base URI, since model URIs are absolute
		in.close();
	}
	
	private ResultSet queryEndpoint(String queryString) {
		Query query = QueryFactory.create(queryString);
		HttpAuthenticator authenticator = new SimpleAuthenticator("dwerft", "#dwerft".toCharArray());
        QueryExecution qExe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query , authenticator);
		return qExe.execSelect();
	}
	
	

	/**
	 * Retrieves a set of resources that have the specified type (ontology class name)
	 * 
	 * @param className
	 * @return
	 */
	public ArrayList<Resource> getResourcesByType(String className) {
		
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
	 * via the specified object property. 
	 * 
	 * @param start
	 * 				Start resource (subject)
	 * @param objectPropertyName
	 * 				Name of the object property
	 * @return Linked resources
	 */
	public ArrayList<Resource> getLinkedResources(Resource start, String objectPropertyName) {
		
		ArrayList<Resource> result = new ArrayList<Resource>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ OntologyConstants.ONTOLOGY_PREFIX+":"+objectPropertyName + " "
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
	public ArrayList<Object> getLinkedDataValues(Resource start, String datatypePropertyName) {
		ArrayList<Object> result = new ArrayList<Object>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "<" + start.getURI() + "> "
				+ OntologyConstants.ONTOLOGY_PREFIX+":"+datatypePropertyName + " "
				+ "?res"
				+ "}";

		
		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getLiteral("res").getValue());
		}
				
		return result;
	}
	
	/**
	 * Gets all scenes of a project
	 * 
	 * @param projectID
	 * @param episodeID
	 * @return a XML String containing all scenes of an episode
	 */
	protected String getScenesAsXML(String projectID) {
		
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
	 * @param q the QuerySolution
	 * @param nodeName the name of the resource/literal
	 * @return the value of the resource/literal or an empty string
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
