package de.werft.examples;


import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * The Class SparqlExample make use of the Jena API.
 * In order to access the information stored in the linked production data cloud, queries must be send to a SPARQL endpoint.
 * This is an example demonstrating how to query the SPARQL endpoint. 
 * The query used fetches the URIs of all projects currently stored in the LPDC.
 */
public class SparqlExample {

	public static void main(String[] args) {
		
		//The URL of the SPARQL endpoint
		String endpoint = "http://sparql.filmontology.org/";
		
		//The query being used in this example fetches all projects currently stored in the lpdc
		String sparqlQuery = "select * from <http://filmontology.org/> where {<http://filmontology.org/resource/DWERFT> ?predicate ?object} ";
 
		//Sending queries requires authentication with a username and password
		HttpAuthenticator authenticator = new SimpleAuthenticator("dwerft", "#dwerft".toCharArray());
        
		//Setting up the objects required by jena 
		Query query = QueryFactory.create(sparqlQuery); 
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query , authenticator);
        
        //Executes the prepared query. A query always returns a result set
        ResultSet results = queryExecution.execSelect();
        
        //Print the result set to console
        ResultSetFormatter.out(System.out, results, query) ;
	}

}
