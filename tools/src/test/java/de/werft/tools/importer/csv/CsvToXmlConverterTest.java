package de.werft.tools.importer.csv;

import org.junit.Test;

/**
 * Test for the csv conversion, verify the correctness by hand.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvToXmlConverterTest {

    private String fileLocation = "src/test/resources/dummy.csv";

    private String aleFileLocation = "examples/A001R2GJ_AVID.ale";

    @Test
    public void testCsvConversion() throws Exception {
        CsvToXmlConverter csvConverter = new CsvToXmlConverter();
        csvConverter.convertToXml(fileLocation, ';');
    }

    @Test
    public void testAleConversion() throws Exception {
        AleToXmlConverter aleConverter = new AleToXmlConverter();
        aleConverter.convertToXml(aleFileLocation, '\t');
    }

}