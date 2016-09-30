package de.werft.tools.importer;

import de.werft.tools.importer.rmllib.Document;
import de.werft.tools.importer.rmllib.RmlMapper;
import org.junit.Test;

import java.net.MalformedURLException;
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

    @Test
    public void testXml() {
        URL file = getClass().getResource("/rml/xml/xml.rml.ttl");
        URL input = getClass().getResource("/rml/xml/xml.xml");
        RmlMapper mapper = new RmlMapper();
        mapper.convertGeneric(new Document(file, input, null));
    }

    @Test
    public void testGeneric() {
        URL file = getClass().getResource("/rml/generic/generic.rml.ttl");
        URL input = getClass().getResource("/rml/generic/generic_example_cast.xml");
        RmlMapper mapper = new RmlMapper();
        mapper.convertGeneric(new Document(file, input, null));
    }

    @Test
    public void testPreproducer() throws MalformedURLException {
        URL file = new URL("file:/home/ratzeputz/Entwicklung/repos/dwerft/tools/mappings/preproducer.rml.ttl");
        RmlMapper mapper = new RmlMapper();
        mapper.convertPreproducer(new Document(file, null, null));
    }

    @Test
    public void testPreproducerStatic() throws MalformedURLException {
        URL file = new URL("file:/home/ratzeputz/Entwicklung/repos/dwerft/tools/mappings/preproducer.rml.ttl");
        URL input = new URL("file:///tmp/prepro7569150280661069441.xml");
        RmlMapper mapper = new RmlMapper();
        mapper.convertGeneric(new Document(file, input, null));
    }
}