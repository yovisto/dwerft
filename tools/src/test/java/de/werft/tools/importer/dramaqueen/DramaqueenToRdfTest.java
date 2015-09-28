package de.werft.tools.importer.dramaqueen;

import java.io.InputStream;

import de.werft.tools.sources.DramaQueenSource;
import org.junit.Test;

import de.werft.tools.general.OntologyConstants;

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
