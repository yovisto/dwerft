package de.werft.tools.importer.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GenerateAleOntologyProperties {

	public static void main(String[] args) {
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File("mappings/ale-columns-properties.txt")));
			
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File("mappings/generated-ale-properties.txt")));
			
			String line = "";
			
			Set<String> mapped = new HashSet<String>();
			
		    while ((line = in.readLine()) != null) {
		    	
		    	String[] split = line.split("\t");
		    	
		    	String prop = split[1];
		    	String desc = split[2];
		    	
		    	if (!mapped.contains(prop)) {
		    	
			        System.out.println(prop);
			        
			        String[] propSplit = prop.split("/");
			        String propName = propSplit[5];
			        
			        wr.write("<owl:DatatypeProperty rdf:about=\"&foo;"+propName+"\">\n");
			    	wr.write("<rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n");
			    	wr.write("<rdfs:label>"+propName+"</rdfs:label>\n");
			    	wr.write("<rdfs:comment>"+desc+"</rdfs:comment>\n");
			    	wr.write("<rdfs:domain rdf:resource=\"&foo;Clip\"/>\n");
			    	wr.write("<rdfs:subPropertyOf rdf:resource=\"&foo;dwerftDataProperty\"/>\n");
			    	wr.write("<rdfs:range rdf:resource=\"&xsd;string\"/>\n");
			    	wr.write("</owl:DatatypeProperty>\n");
			    	wr.write("\n");
		    	
			    	mapped.add(prop);
		    	}
	
		    }
		    
		    in.close();
		    wr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
