package de.werft.tools.importer.dramaqueen;

import java.io.InputStream;

import de.werft.tools.sources.DramaQueenSource;
import org.junit.Test;

import de.werft.tools.general.OntologyConstants;
import de.werft.tools.importer.general.RdfGenerator;

public class DramaQueenConverterTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.dq";
	
	@Test
	public void testDramaQueenConverter() {
		DramaQueenConverter dqc = new DramaQueenConverter(OntologyConstants.ONTOLOGY_FILE, OntologyConstants.ONTOLOGY_FORMAT);
		
		DramaQueenSource dqSource = new DramaQueenSource();
		InputStream is = dqSource.get(dqFile);
		if (is != null) {
			dqc.generate(is);
			RdfGenerator.writeRDFToFile(dqc.getGeneratedModel(), "examples/Hansel_Gretel_de.ttl");
//			dqc.getGeneratedModel().write(System.out, "TTL");
		}
	}

}
