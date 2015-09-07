package de.dwerft.lpdc.importer.dramaqueen;

import java.io.InputStream;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.sources.DramaQueenSource;

public class DramaqueenToRdfTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.dq";
	private static final String outputFile = "examples/Hansel_Gretel_de_new.ttl";
	private static final String DRAMAQUEEN_MAPPINGS_FILE = "src/main/resource/dramaqueen.mappings";


	@Test
	public void testConverter() {
		
		DramaQueenSource dqsoure = new DramaQueenSource();
		InputStream inputStream = dqsoure.get(dqFile);
		
		
		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				DRAMAQUEEN_MAPPINGS_FILE);
		
		dqrdf.convert(inputStream);
		
		dqrdf.writeRdfToFile(outputFile);

//		Model generatedModel = dqrdf.getGeneratedModel();
//		generatedModel.write(System.out, "TTL");
	}

}
