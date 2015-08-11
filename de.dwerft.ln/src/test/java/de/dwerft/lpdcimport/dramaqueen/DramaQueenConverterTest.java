package de.dwerft.lpdcimport.dramaqueen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.dramaqueen.DramaQueenConverter;
import de.dwerft.lpdc.importer.general.RdfGenerator;

public class DramaQueenConverterTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.xml";
	
	@Test
	public void testDramaQueenConverter() {
		DramaQueenConverter dqc = new DramaQueenConverter(OntologyConstants.ONTOLOGY_FILE, OntologyConstants.ONTOLOGY_FORMAT);
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(dqFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (fis != null) {
			dqc.generate(fis);
		}
		
		RdfGenerator.writeRDFToFile(dqc.getGeneratedModel(), "examples/Hansel_Gretel_de.ttl");
		
		dqc.getGeneratedModel().write(System.out, "TTL");
		
	}

}
