package de.werft.tools.importer;

import de.werft.tools.general.DwerftConfig;
import de.werft.tools.rmllib.Document;
import de.werft.tools.rmllib.RmlMapper;
import org.aeonbits.owner.ConfigFactory;
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
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convert(new Document(file, null, null));
    }

    @Test
    public void testCsv() {
        URL file = getClass().getResource("/rml/csv/csv.rml.ttl");
        URL input = getClass().getResource("/rml/csv/csv_dummy.csv");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertCsv(new Document(file, input, null));
    }

    @Test
    public void testAle() {
        URL file = getClass().getResource("/rml/ale/ale.rml.ttl");
        URL input = getClass().getResource("/rml/ale/ale_dummy.ale");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertAle(new Document(file, input, null));
    }

    @Test
    public void testXml() {
        URL file = getClass().getResource("/rml/xml/xml.rml.ttl");
        URL input = getClass().getResource("/rml/xml/xml.xml");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertGeneric(new Document(file, input, null));
    }

    @Test
    public void testGeneric() {
        URL file = getClass().getResource("/rml/generic/generic.rml.ttl");
        URL input = getClass().getResource("/rml/generic/generic_example_cast.xml");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertGeneric(new Document(file, input, null));
    }

    @Test
    public void testPreproducer() throws MalformedURLException {
        URL file = getClass().getResource("/rml/preproducer.rml.ttl");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertPreproducer(new Document(file, null, null));
    }

    @Test
    public void testDramaqueen() throws MalformedURLException {
        URL file = getClass().getResource("/rml/dq/dramaqueen.rml.ttl");
        URL input = getClass().getResource("/rml/dq/hansel_gretel.dq");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertDramaqueen(new Document(file, input, null));
    }

}