package de.dwerft.ln;


import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


public class SparqlExample {

	public static void main(String[] args) {
		
		String endpoint = "http://sparql.filmontology.org/";
		String sparqlQuery = "select * from <http://filmontology.org/> where {<http://filmontology.org/resource/DWERFT> ?p ?o} ";
 
		HttpAuthenticator authenticator = new SimpleAuthenticator("dwerft", "#dwerft".toCharArray());
        Query query = QueryFactory.create(sparqlQuery); 
        QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query , authenticator);
        ResultSet results = qExe.execSelect();
        ResultSetFormatter.out(System.out, results, query) ;
	}

}
