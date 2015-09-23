package de.dwerft.lpdc.importer.preproducer;

import java.io.File;

import org.junit.Test;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.sources.PreproducerSource;

public class PreProducerToRdfTest {
	
	private static final String PREPRODUCER_CONFIG_FILE = "src/main/resources/config.properties";
	private static final String PREPRODUCER_MAPPINGS_FILE = "src/main/resources/preproducer.mappings";
	private static final String outputFile = "examples/preproducer_export_new.ttl";

	@Test
	public void testConverter() {
		
		PreproducerSource pps = new PreproducerSource(new File(PREPRODUCER_CONFIG_FILE));
		
		PreProducerToRdf pprdf = new PreProducerToRdf(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				PREPRODUCER_MAPPINGS_FILE);
		
		pprdf.convert(pps.get("info"));
		pprdf.convert(pps.get("listCharacters"));
		pprdf.convert(pps.get("listCrew"));
		pprdf.convert(pps.get("listDecorations"));
		pprdf.convert(pps.get("listExtras"));
		pprdf.convert(pps.get("listFigures"));
		pprdf.convert(pps.get("listScenes"));
		pprdf.convert(pps.get("listSchedule"));
		
//		Model generatedModel = pprdf.getGeneratedModel();
		
//		generatedModel.write(System.out, "TTL");
		
		pprdf.writeRdfToFile(outputFile);

		// upload to triplestore
//		TripleStoreSource triple = new TripleStoreSource();
//		triple.send(outputFile);
	}
}
