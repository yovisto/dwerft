package de.werft.examples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateAleMappings {

	public static void main(String[] args) {
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File("ontology/ale-columns-properties.txt")));
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File("mappings/ale_new.rml.ttl")));
			
			wr.write("@prefix rr: <http://www.w3.org/ns/r2rml#>.\n"
					+ "@prefix rml: <http://semweb.mmlab.be/ns/rml#> .\n"
					+ "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n"
					+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n"
					+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
					+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.\n"
					+ "@prefix foo: <http://filmontology.org/ontology/2.0/> .\n"
					+ "@prefix schema: <http://schema.org/>.\n"
					+ "@prefix csvw: <http://www.w3.org/ns/csvw#>.\n"
					+ "\n"
					+ "<#ClipMapping>\n"
					+ "rml:logicalSource [\n"
					+ "rml:source [\n"
					+ "a csvw:Table;\n"
					+ "csvw:dialect [\n"
					+ "a csvw:Dialect;\n"
					+ "csvw:delimiter \";\";\n"
					+ "csvw:encoding \"UTF-8\";\n"
					+ "csvw:header \"1\"^^xsd:boolean;\n"
					+ "csvw:headerRowCount \"1\"^^xsd:nonNegativeInteger;\n"
					+ "csvw:trim \"1\"^^xsd:boolean;\n"
					+ "] ];\n"
					+ "rml:referenceFormulation ql:CSV;\n"
					+ "];\n"
					+ "\n"
					+ "rr:subjectMap [\n"
					+ "rr:template \"http://filmontology.org/resource/Clip/{UUID}\";\n"
					+ "rr:class foo:Clip;\n"
					+ "];\n");
			
			String line = "";
			
		    while ((line = in.readLine()) != null) {
		    	String[] split = line.split("\t");
		    	
		    	String name = split[0];
		    	String prop = split[1];
		    	
		    	String[] propSplit = prop.split("/");
		    	
		    	String propName = propSplit[propSplit.length-1];
		    	
		    	
		    	System.out.println(name);
		    	
		    	 wr.write("rr:predicateObjectMap ["+"\n"
		    	 		+ "rr:predicate foo:"+propName+";"+"\n"
		    	 		+ "rr:objectMap ["+"\n"
		    	 		+ "rml:reference \""+name+"\";"+"\n"
		    	 		+ "rr:datatype xsd:string;"+"\n"
		    	 		+ "];"+"\n"
		    	 		+ "];"+"\n"+"\n");
		    
		    }
		    	
//		    	
//		    	name = name
//                        .replace("#", " number")
//                        .replace("(", "open ").replace(")", " close")
//                        .replace("2nd", "second")
//                        .replace("/", " slash ")
//                        .trim()
//                        .replace(" ", "__");
//		    	
//		        System.out.println(name);
//		        
//		        wr.write("map"+i+".xmlNodePath=/csv/row/"+name+"\n");
//		        wr.write("map"+i+".conditionalAttributeName=\n");
//		        wr.write("map"+i+".conditionalAttributeName=\n");
//		        wr.write("map"+i+".contentSource=TEXT_CONTENT\n");
//		        wr.write("map"+i+".contentElementName=\n");
//		        wr.write("map"+i+".targetOntologyClass=http://filmontology.org/ontology/1.0/Clip\n");
//		        wr.write("map"+i+".targetOntologyProperty="+prop+"\n");
//		        wr.write("map"+i+".targetPropertyType=DATATYPE_PROPERTY\n");
//		        
//		        i++;
//		    }
		    
		    in.close();
		    wr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
