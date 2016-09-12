package de.werft.tools.importer;

import de.werft.tools.importer.rmllib.Document;
import de.werft.tools.importer.rmllib.RmlMapper;
import org.junit.Test;

import java.net.URL;

/**
 * Created by ratzeputz on 02.09.16.
 */
public class RmlMapperTest {

    @Test
    public void testRml() {
        URL file = getClass().getResource("/rml/test/test.rml.ttl");
        RmlMapper mapper = new RmlMapper();
        mapper.convert(new Document(file, null, null));
    }

    @Test
    public void testCsv() {
        URL file = getClass().getResource("/rml/csv/csv.rml.ttl");
        URL input = getClass().getResource("/rml/csv/csv_dummy.csv");
        RmlMapper mapper = new RmlMapper();
        mapper.convertCsv(new Document(file, input, null));
    }

    @Test
    public void testAle() {
        URL file = getClass().getResource("/rml/ale/ale.rml.ttl");
        URL input = getClass().getResource("/rml/ale/ale_dummy.ale");
        RmlMapper mapper = new RmlMapper();
        mapper.convertAle(new Document(file, input, null));
    }
}