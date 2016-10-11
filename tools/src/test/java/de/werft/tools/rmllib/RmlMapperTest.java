package de.werft.tools.rmllib;

import de.werft.tools.general.DwerftConfig;
import de.werft.tools.general.Document;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * Test the {@link RmlMapper} which utilizes the RML library
 * for mapping structured data into linked data.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class RmlMapperTest {

    @Test
    public void testCsv() throws IOException {
        URL file = getClass().getResource("/rml/csv/csv.rml.ttl");
        URL input = getClass().getResource("/rml/csv/csv_dummy.csv");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertCsv(new Document(file, input, getTempFile()));
    }

    @Test
    public void testAle() throws IOException {
        URL file = getClass().getResource("/rml/ale/ale.rml.ttl");
        URL input = getClass().getResource("/rml/ale/ale_dummy.ale");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertAle(new Document(file, input, getTempFile()));
    }

    @Test
    public void testXml() throws IOException {
        URL file = getClass().getResource("/rml/xml/xml.rml.ttl");
        URL input = getClass().getResource("/rml/xml/xml.xml");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertGeneric(new Document(file, input, getTempFile()));
    }

    @Test
    public void testGeneric() throws IOException {
        URL file = getClass().getResource("/rml/generic/generic.rml.ttl");
        URL input = getClass().getResource("/rml/generic/generic_example_cast.xml");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertGeneric(new Document(file, input, getTempFile()));
    }

    @Test
    public void testPreproducer() throws IOException {
        URL file = getClass().getResource("/rml/preproducer.rml.ttl");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertPreproducer(new Document(file, null, getTempFile()));
    }

    @Test
    public void testDramaqueen() throws IOException {
        URL file = getClass().getResource("/rml/dq/dramaqueen.rml.ttl");
        URL input = getClass().getResource("/rml/dq/hansel_gretel.dq");
        RmlMapper mapper = new RmlMapper(ConfigFactory.create(DwerftConfig.class));
        mapper.convertDramaqueen(new Document(file, input, getTempFile()));
    }

    /* return a temporary output file */
    private URL getTempFile() throws IOException {
        return Files.createTempFile("out", ".ttl").toUri().toURL();
    }
}