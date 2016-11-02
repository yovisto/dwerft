package de.werft.tools.rmllib.preprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test the basic functionality of the basic preprocessor or
 * more specific test that the sources files are inserted correctly.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class BasicPreprocessorTest {

    @Test
    public void preprocessMapping() throws Exception {
        BasicPreprocessor basic = new BasicPreprocessor("");
        URL actualMapping = basic.preprocessMapping(createMapping(), new URL("file:///test"));
        Assert.assertTrue(compareMappings(actualMapping));
    }

    /* create a temp file with the mapping template */
    private URL createMapping() throws IOException {
        Path tmp = Files.createTempFile("test", ".ttl");
        Files.write(tmp, testMapping.getBytes());
        return tmp.toUri().toURL();
    }

    /* load two mappings as models and compare them */
    private boolean compareMappings(URL actualMapping) {
        Model actual = RDFDataMgr.loadModel(actualMapping.toString());
        Model expected = ModelFactory.createDefaultModel();
        RDFDataMgr.read(expected, new ByteArrayInputStream(expectedMapping.getBytes()), Lang.TURTLE);
        return expected.isIsomorphicWith(actual);
    }

    /* define the mapping before preprocessing and the  output */
    private String testMapping = "@prefix rr: <http://www.w3.org/ns/r2rml#>.\n" +
            "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
            "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
            "@prefix mail: <http://example.com/mail#>.\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
            "@prefix ex: <http://www.example.com/> .\n" +
            "@base <http://example.com/base> .\n" +
            "" +
            "<#FromToMaps>\n" +
            "    rml:logicalSource [\n" +
            "        rml:iterator \"/notes/note\";\n" +
            "        rml:referenceFormulation ql:XPath;\n" +
            "    ];\n" +
            "    rr:subjectMap [\n" +
            "        rr:template \"http://www.example.com/note/{@id}\";\n" +
            "        rr:class mail:note;\n" +
            "    ].\n";

    private String expectedMapping = "@prefix rr: <http://www.w3.org/ns/r2rml#>.\n" +
            "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
            "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
            "@prefix mail: <http://example.com/mail#>.\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
            "@prefix ex: <http://www.example.com/> .\n" +
            "@base <http://example.com/base> .\n" +
            "" +
            "<#FromToMaps>\n" +
            "    rml:logicalSource [\n" +
            "        rml:source \"/test\";\n" +
            "        rml:iterator \"/notes/note\";\n" +
            "        rml:referenceFormulation ql:XPath;\n" +
            "    ];\n" +
            "    rr:subjectMap [\n" +
            "        rr:template \"http://www.example.com/note/{@id}\";\n" +
            "        rr:class mail:note;\n" +
            "    ].\n";
}