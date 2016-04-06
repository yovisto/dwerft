package de.werft.tools.importer.csv;

import org.junit.Test;

import java.io.File;

/**
 * Test for the csv conversion, verify the correctness by hand.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvToXmlConverterTest {

    private String csvFileLocation = "src/test/resources/files/csv_dummy.csv";

    private String aleFileLocation = "src/test/resources/files/ale_dummy.ale";

    //@After
    public void tearDown() {
        new File(csvFileLocation).delete();
        new File(aleFileLocation).delete();
    }


    @Test
    public void testCsvConversion() throws Exception {
        CsvToXmlConverter csvConverter = new CsvToXmlConverter();
        csvConverter.convertToXml(csvFileLocation, ';');
    }

    @Test
    public void testAleConversion() throws Exception {
        AleToXmlConverter aleConverter = new AleToXmlConverter();
        aleConverter.convertToXml(aleFileLocation, '\t');
    }

}