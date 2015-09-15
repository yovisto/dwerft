package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;

public class ExporterTest {

	private final String tmpDir = System.getProperty("java.io.tmpdir");
	private final File ontologyFile = new File(tmpDir + "/filmontology_example.ttl");
	
	@Test
	public void testLockitExporter() throws IOException {
		
		LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT, OntologyConstants.ONTOLOGY_FILE, tmpDir + "/lockit_filmontology_scenes.csv", "17621");
		e.export();
	}
	
	//@Test
//	public void testPreproducerExporter() throws IOException {
//		
//		PreproducerExporter e = new PreproducerExporter(ontologyFile, mapping, tmpDir + "/preproducer_filmontology_scenes.csv", "");
//	}
}
