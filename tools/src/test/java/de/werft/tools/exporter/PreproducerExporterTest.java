package de.werft.tools.exporter;

import de.werft.tools.general.OntologyConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class PreproducerExporterTest {

    private final String tmpDir = "examples";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(new java.io.File("ontology/dwerft-ontology.owl"));
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
