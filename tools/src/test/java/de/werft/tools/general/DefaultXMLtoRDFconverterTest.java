package de.werft.tools.general;

import de.werft.tools.DwerftUtils;
import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class DefaultXMLtoRDFconverterTest {
	
	private static final String exampleXMLfile = "examples/generic_example.xml";
	private static final String outputFile = "examples/generic_example.ttl";
	private static final String MAPPINGS_FILE = "mappings/generic_example.mappings";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(new java.io.File("ontology/dwerft-ontology.owl"));
    }

	@Test
	public void testConverter() throws IOException {
		DefaultXMLtoRDFconverter converter = new DefaultXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				MAPPINGS_FILE);
		
		converter.convert(exampleXMLfile);
        DwerftUtils.writeRdfToConsole(converter.getResult());
	}


}
