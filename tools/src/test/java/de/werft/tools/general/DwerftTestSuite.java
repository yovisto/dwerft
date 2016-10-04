package de.werft.tools.general;

import de.werft.tools.old.sources.MapperTest;
import de.werft.tools.old.sources.csv.CsvToXmlConverterTest;
import de.werft.tools.old.sources.dramaqueen.DramaqueenToRdfTest;
import de.werft.tools.old.sources.general.DefaultXMLtoRDFconverter;
import de.werft.tools.old.sources.preproducer.PreProducerToRdfTest;
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
