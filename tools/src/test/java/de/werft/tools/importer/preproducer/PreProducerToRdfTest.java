package de.werft.tools.importer.preproducer;

import de.werft.tools.general.DwerftConfig;
import de.werft.tools.general.OntologyConstants;
import de.werft.tools.sources.PreproducerSource;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * PreProducer Tests
 *
 * At this moment there is only a execution tests. Please validate the output by hand.
 *
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PreProducerToRdfTest {
	
	private static final String PREPRODUCER_MAPPINGS_FILE = "mappings/preproducer.mappings";
	private static final String outputFile = "examples/preproducer_export.ttl";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(new java.io.File("ontology/dwerft-ontology.owl"));
    }

	@Test
    public void testConverter() throws FileNotFoundException {
        DwerftConfig config = ConfigFactory.create(DwerftConfig.class);
		PreproducerSource pps = new PreproducerSource(config.getPreProducerKey(), config.getPreProducerSecret(),
            config.getPreProducerAppSecret());
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
