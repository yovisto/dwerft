package de.werft.tools.importer.dramaqueen;

import de.werft.tools.DwerftUtils;
import de.werft.tools.general.OntologyConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 *  DramaQueen Tests
 *
 *  At this moment there is only a simple execution tests with validation by hand.
 *
 * @author
 */
public class DramaqueenToRdfTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.dq";
	private static final String outputFile = "examples/Hansel_Gretel_de.ttl";
	private static final String DRAMAQUEEN_MAPPINGS_FILE = "mappings/dramaqueen.mappings";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(new java.io.File("ontology/dwerft-ontology.owl"));
    }

	@Test
	public void testConverter() throws IOException {
		// test the convertion and validate the result by hand
		//InputStream inputStream = new DramaQueenSource().get(dqFile);

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				DRAMAQUEEN_MAPPINGS_FILE);
		
		dqrdf.convert(dqFile);
        DwerftUtils.writeRdfToConsole(dqrdf.getResult());
//		Model generatedModel = dqrdf.getResult();
//		generatedModel.write(System.out, "TTL");
	}

}
