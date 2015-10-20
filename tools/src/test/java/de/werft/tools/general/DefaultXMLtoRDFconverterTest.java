package de.werft.tools.general;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;

public class DefaultXMLtoRDFconverterTest {
	
	private static final String exampleXMLfile = "examples/generic_example.xml";
	private static final String outputFile = "examples/generic_example.ttl";
	private static final String MAPPINGS_FILE = "src/main/resources/generic_example.mappings";
	
	@Test
	public void testConverter() {
		
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(exampleXMLfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DefaultXMLtoRDFconverter converter = new DefaultXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				MAPPINGS_FILE);
		
		converter.convert(inputStream);
		
		converter.writeRdfToFile(outputFile);
		
		
	}


}
