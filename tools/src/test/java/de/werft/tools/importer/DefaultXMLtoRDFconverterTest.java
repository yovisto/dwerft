package de.werft.tools.importer;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.general.AbstractTest;
import de.werft.tools.general.OntologyConstants;
import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DefaultXMLtoRDFconverterTest extends AbstractTest {

    private final String xmlFile = testFolder + "generic_example.xml";

    private final String veriFile = verificationFolder + "generic_example.ttl";

	private static final String MAPPINGS_FILE = "mappings/generic_example_cast.mappings";

    @Override
    public void setUp() {
        OntologyConstants.setOntologyFile(conf.getOntologyFile());
    }

    @Override
    public void tearDown() { }

    @Test
	public void testConverter() throws IOException {
		DefaultXMLtoRDFconverter converter = new DefaultXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				MAPPINGS_FILE);

        converter.convert(xmlFile);
        Model m = RDFDataMgr.loadModel(veriFile);
        assertTrue(m.isIsomorphicWith(converter.getResult()));
	}
}
