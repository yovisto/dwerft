package de.werft.tools.old.sources.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateAleMappings {

	public static void main(String[] args) {
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File("mappings/ale-columns-properties.txt")));
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File("mappings/ale_xml_columns.mappings")));
			
			wr.write("map1.xmlNodePath=/csv/row\n");
			wr.write("map1.conditionalAttributeName=\n");
			wr.write("map1.conditionalAttributeValue=\n");
			wr.write("map1.contentSource=\n");
			wr.write("map1.contentElementName=\n");
			wr.write("map1.targetOntologyClass=http://filmontology.org/ontology/1.0/Clip\n");
			wr.write("map1.targetOntologyProperty=\n");
			wr.write("map1.targetPropertyType=\n");

			
			String line = "";
			
			int i = 2;
			
		    while ((line = in.readLine()) != null) {
		    	
		    	String[] split = line.split("\t");
		    	
		    	String name = split[0];
		    	String prop = split[1];
		    	
		    	name = name
                        .replace("#", " number")
                        .replace("(", "open ").replace(")", " close")
                        .replace("2nd", "second")
                        .replace("/", " slash ")
                        .trim()
                        .replace(" ", "__");
		    	
		        System.out.println(name);
		        
		        wr.write("map"+i+".xmlNodePath=/csv/row/"+name+"\n");
		        wr.write("map"+i+".conditionalAttributeName=\n");
		        wr.write("map"+i+".conditionalAttributeName=\n");
		        wr.write("map"+i+".contentSource=TEXT_CONTENT\n");
		        wr.write("map"+i+".contentElementName=\n");
		        wr.write("map"+i+".targetOntologyClass=http://filmontology.org/ontology/1.0/Clip\n");
		        wr.write("map"+i+".targetOntologyProperty="+prop+"\n");
		        wr.write("map"+i+".targetPropertyType=DATATYPE_PROPERTY\n");
		        
		        i++;
		    }
		    
		    in.close();
		    wr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
