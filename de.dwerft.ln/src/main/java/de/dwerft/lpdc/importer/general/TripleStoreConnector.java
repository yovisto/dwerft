package de.dwerft.lpdc.importer.general;

import java.util.ArrayList;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.web.DatasetGraphAccessorHTTP;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.graph.GraphSPARQL;
import com.hp.hpl.jena.sparql.graph.GraphSPARQLService;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import de.dwerft.lpdc.general.OntologyConstants;

public class TripleStoreConnector {
	
	private OntologyConnector ontConn = null;
	private String sparqlEndpointUrl = null;

	public TripleStoreConnector(OntologyConnector ontConn, String sparqlEndpointUrl) {
		this.ontConn = ontConn;
		this.sparqlEndpointUrl = sparqlEndpointUrl;
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
		Query query = QueryFactory.create(queryString);
		HttpAuthenticator authenticator = new SimpleAuthenticator(
				OntologyConstants.SPARQL_ENDPOINT_USER,
				OntologyConstants.SPARQL_ENDPOINT_PASSWORD.toCharArray());
        QueryExecution qExe = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query , authenticator);
		return qExe.execSelect();
	}
	
	/**
	 * Runs an update over the sparql virtuoso endpoint
	 *
	 * @param file the file  you want to upload
	 */
	public void updateEndpoint(String file) {
		UpdateRequest request = UpdateFactory.create();
		request.add("DROP ALL").add("CREATE GRAPH <http://filmontology.org>")
			.add("LOAD <file:///examples/preproducer_export_new.ttl> INTO GRAPH <http://filmontology.org>") ;
        	
		System.out.println(request.toString());
		
		HttpAuthenticator authenticator = new SimpleAuthenticator(
				OntologyConstants.SPARQL_ENDPOINT_USER,
				OntologyConstants.SPARQL_ENDPOINT_PASSWORD.toCharArray());
		
		UpdateProcessor qExe = UpdateExecutionFactory.createRemote(request, sparqlEndpointUrl, authenticator);
		qExe.execute();
	}
	
	
	/**
	 * Retrieves a set of resources that have the specified type (ontology class name)
	 * 
	 * @param classUri
	 * 					URI of the ontology class
	 * @return All resources that have a RDF type relation to the ontology class
	 */
	public ArrayList<Resource> getResourcesByType(String classUri) {
		
		ArrayList<Resource> result = new ArrayList<Resource>();
		
		String query = OntologyConstants.ONTOLOGY_PREFIXES 
				+ "select ?res where { "
				+ "?res rdf:type "+classUri
				+ "}";

		ResultSet rs = queryEndpoint(query);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			result.add(sol.getResource("res"));
		}
		
		return result;
		
	}

	public String getSparqlEndpointUrl() {
		return sparqlEndpointUrl;
	}
}
