package de.werft.tools.importer.dramaqueen;

import java.io.InputStream;

import de.werft.tools.sources.DramaQueenSource;
import org.junit.Test;

import de.werft.tools.general.OntologyConstants;

/**
 *  DramaQueen Tests
 *
 *  At this moment there is only a simple execution tests with validation by hand.
 *
 * @author
 */
public class DramaqueenToRdfTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.dq";
	private static final String outputFile = "examples/Hansel_Gretel_de_new2.ttl";
	private static final String DRAMAQUEEN_MAPPINGS_FILE = "src/main/resources/dramaqueen.mappings";

	@Test
	public void testConverter() {
		// test the convertion and validate the result by hand
		InputStream inputStream = new DramaQueenSource().get(dqFile);

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
