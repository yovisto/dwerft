package de.dwerft.lpdcimport.dramaqueen;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.dramaqueen.DramaQueenConverter;

public class DramaQueenConverterTest {
	
	@Test
	public void testDramaQueenConverter() {
		DramaQueenConverter dqc = new DramaQueenConverter(OntologyConstants.ONTOLOGY_FILE, OntologyConstants.ONTOLOGY_FORMAT);
		System.out.println("Test");
	}

}
