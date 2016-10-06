package de.werft.tools.exporter;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Test export functions.
 *
 * At this moment all tests are only execution tests.
 * Please prove the results by hand.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
public class LockitExporterTest {

	private final String tmpDir = "examples";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(new java.io.File("ontology/dwerft-ontology.owl"));
    }
	
	@Test
	public void testLockitExporter() throws IOException {
		LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT,
				OntologyConstants.ONTOLOGY_FILE, tmpDir + "/lockit_filmontology_scenes.csv", "17621");
		e.export();
        assertTrue(new File(tmpDir + "/lockit_filmontology.scenes.csv").exists());
	}
}
