package de.werft.tools.importer.csv;

import de.werft.tools.general.AbstractTest;
import org.junit.Test;

import java.io.File;

/**
 * Test for the csv conversion, verify the correctness by hand.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvToXmlConverterTest extends AbstractTest {

    private String csvFileLocation = testFolder + "csv_dummy.csv";

    private String aleFileLocation = testFolder + "ale_dummy.ale";

    //@After
    public void tearDown() {
        new File(replaceSuffix(csvFileLocation, "xml")).deleteOnExit();
        new File(replaceSuffix(aleFileLocation, "xml")).deleteOnExit();
    }

    // both checks only that the conversion takes place
    @Test
    public void testCsvConversion() throws Exception {
        CsvToXmlConverter csvConverter = new CsvToXmlConverter(';');
        csvConverter.convert(csvFileLocation);
    }

    @Test
    public void testAleConversion() throws Exception {
        AleToXmlConverter aleConverter = new AleToXmlConverter('\t');
        aleConverter.convert(aleFileLocation);
    }
}