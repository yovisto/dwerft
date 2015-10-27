package de.werft.tools.importer.preproducer;

import java.io.File;
import java.io.FileNotFoundException;

import de.werft.tools.general.OntologyConstants;
import de.werft.tools.sources.PreproducerSource;
import org.junit.Test;

/**
 * PreProducer Tests
 *
 * At this moment there is only a execution tests. Please validate the output by hand.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
public class PreProducerToRdfTest {
	
	private static final String PREPRODUCER_CONFIG_FILE = "src/main/resources/config.properties";
	private static final String PREPRODUCER_MAPPINGS_FILE = "src/main/resources/mappings/preproducer.mappings";
	private static final String outputFile = "examples/preproducer_export.ttl";

	@Test
    public void testConverter() throws FileNotFoundException {
		PreproducerSource pps = new PreproducerSource(new File(PREPRODUCER_CONFIG_FILE));
		PreProducerToRdf pprdf = new PreProducerToRdf(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				PREPRODUCER_MAPPINGS_FILE);

        // the order is important, you have to create all classes before
        // you can use them.
		pprdf.convert(pps.get("info"));
		pprdf.convert(pps.get("listCharacters"));
		pprdf.convert(pps.get("listCrew"));
		pprdf.convert(pps.get("listDecorations"));
		pprdf.convert(pps.get("listExtras"));
		pprdf.convert(pps.get("listFigures"));
		pprdf.convert(pps.get("listScenes"));
		pprdf.convert(pps.get("listSchedule"));
		pprdf.writeRdfToFile(outputFile);
	}
}
