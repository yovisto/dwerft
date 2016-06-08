package de.werft.tools.importer.preproducer;

import de.werft.tools.DwerftUtils;
import de.werft.tools.general.AbstractTest;
import de.werft.tools.general.OntologyConstants;
import de.werft.tools.sources.PreproducerSource;
import org.apache.jena.riot.Lang;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * PreProducer Tests
 *
 * At this moment there is only a execution tests. Please validate the output by hand.
 *
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PreProducerToRdfTest extends AbstractTest {
	
	private static final String mapping = "mappings/preproducer.mappings";

    @Before
    public void setUp() {
        OntologyConstants.setOntologyFile(conf.getOntologyFile());
    }

    @Test
    public void testConverter() throws IOException {
        PreproducerSource pps = new PreproducerSource(conf.getPreProducerKey(), conf.getPreProducerSecret(),
            conf.getPreProducerAppSecret());
		PreProducerToRdf pprdf = new PreProducerToRdf(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				mapping, pps);

        // since the conversion precess generates uuids a formal verification is a
        // bit tricky and not done. the result is printed on a console for validations by hand.
        pprdf.convert("");
        DwerftUtils.writeRdfToConsole(pprdf.getResult(), Lang.TTL);
	}
}
