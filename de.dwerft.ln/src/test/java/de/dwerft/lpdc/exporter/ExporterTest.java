package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.IOException;

public class ExporterTest {

	private final String tmpDir = System.getProperty("java.io.tmpdir");
	private final File ontologyFile = new File(tmpDir + "/filmontology_example.ttl");
	
	//@Test
	public void testLockitExporter() throws IOException {
		
		LockitExporter e = new LockitExporter(ontologyFile, tmpDir + "/lockit_filmontology_scenes.csv");
		e.export();
	}
}