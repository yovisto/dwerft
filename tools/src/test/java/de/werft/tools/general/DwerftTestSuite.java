package de.werft.tools.general;

import de.werft.tools.importer.MapperTest;
import de.werft.tools.importer.csv.CsvToXmlConverterTest;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdfTest;
import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;
import de.werft.tools.importer.preproducer.PreProducerToRdfTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Dwerft test suite
 *
 * All test classes collected.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
@RunWith(Suite.class)
@SuiteClasses({ MapperTest.class, CsvToXmlConverterTest.class, DefaultXMLtoRDFconverter.class,
        PreProducerToRdfTest.class, DramaqueenToRdfTest.class})
public class DwerftTestSuite { }
