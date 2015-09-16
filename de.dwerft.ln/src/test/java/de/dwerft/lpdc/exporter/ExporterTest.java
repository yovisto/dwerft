package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.sources.PreproducerSource;

public class ExporterTest {

	private final String tmpDir = System.getProperty("java.io.tmpdir");
	private final File ontologyFile = new File(tmpDir + "/filmontology_example.ttl");
	
	@Test
	public void testLockitExporter() throws IOException {
		
		LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT, OntologyConstants.ONTOLOGY_FILE, tmpDir + "/lockit_filmontology_scenes.csv", "17621");
		e.export();
	}
	
<<<<<<< HEAD
	@Test
	public void testPreproducerExporter() throws IOException {
		
		String outputPath = tmpDir + "/preproducer_filmontology_scenes.xml";
		PreproducerExporter e = new PreproducerExporter(OntologyConstants.SPARQL_ENDPOINT, OntologyConstants.ONTOLOGY_FILE, outputPath, "17621");
		e.export();
		
		PreproducerSource prpSource = new PreproducerSource(new File("src/main/resource/config.preproducer"));
		prpSource.send(new String(Files.readAllBytes(Paths.get(outputPath))));
	}
=======
	//@Test
//	public void testPreproducerExporter() throws IOException {
//		
//		PreproducerExporter e = new PreproducerExporter(ontologyFile, mapping, tmpDir + "/preproducer_filmontology_scenes.csv", "");
//	}
>>>>>>> 209267e5919b45bdfa5de6a82b11994172c2fc48
}
