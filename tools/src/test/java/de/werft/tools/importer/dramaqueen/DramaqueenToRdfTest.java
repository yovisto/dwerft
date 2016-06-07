package de.werft.tools.importer.dramaqueen;

import de.werft.tools.DwerftUtils;
import de.werft.tools.general.AbstractTest;
import de.werft.tools.general.OntologyConstants;
import org.junit.Test;

import java.io.IOException;

/**
 *  DramaQueen Tests
 *
 *  At this moment there is only a simple execution tests with validation by hand.
 *
 * @author
 */
public class DramaqueenToRdfTest extends AbstractTest {

    private final String inputFile = testFolder + "hansel_gretel.dq";

    private static final String mapping = "mappings/dramaqueen.mappings";

    @Override
    public void setUp() {
        OntologyConstants.setOntologyFile(conf.getOntologyFile());
    }

    @Override
    public void tearDown() { }

    @Test
	public void testConverter() throws IOException {
		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				mapping);
		
		dqrdf.convert(inputFile);
        // since the conversion precess generates uuids a formal verification is a
        // bit tricky and not done. the result is printed on a console for validations by hand.
        DwerftUtils.writeRdfToConsole(dqrdf.getResult());
	}
}
