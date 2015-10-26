package de.werft.tools.exporter;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.werft.tools.general.OntologyConstants;

import static org.junit.Assert.assertTrue;

/**
 * Test export functions.
 *
 * At this moment all tests are only execution tests.
 * Please prove the results by hand.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
public class ExporterTest {

	private final String tmpDir = "examples";
	
	//@Test
	public void testLockitExporter() throws IOException {
		LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT,
				OntologyConstants.ONTOLOGY_FILE, tmpDir + "/lockit_filmontology_scenes.csv", "17621");
		e.export();
        assertTrue(new File(tmpDir + "/lockit_filmontology.scenes.csv").exists());
	}
	
	@Test
	public void testPreproducerExporter() throws IOException {
		
		String outputPath = tmpDir + "/preproducer_filmontology_scenes.xml";
		PreproducerExporter e = new PreproducerExporter(
				OntologyConstants.SPARQL_ENDPOINT,
				OntologyConstants.ONTOLOGY_FILE,
				outputPath, "9860f0bb-d9a6-45e4-9d03-79e7fefd16fa", "17621");
		e.export();
        assertTrue(new File(tmpDir + "/preproducer_filmontology_scenes.xml").exists());
	}


}
