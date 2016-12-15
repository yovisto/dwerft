package de.werft.tools.rmllib.preprocessing;

/**
 * Ensure that the input file is correctly inserted into
 * the mapping file.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvPreprocessorTest extends BasicPreprocessorTest {

    /* define the mapping before preprocessing and the  output */
    @Override
    protected String testMapping() {
        return "@prefix rr: <http://www.w3.org/ns/r2rml#>.\n" +
                "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
                "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
                "@prefix mail: <http://example.com/mail#>.\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
                "@prefix ex: <http://www.example.com/> .\n" +
                "@base <http://example.com/base> .\n" +
                "@prefix csvw: <http://www.w3.org/ns/csvw#>." +
                "" +
                "<#FromToMaps>\n" +
                "    rml:logicalSource [\n" +
                "    rml:source [\n" +
                "    a csvw:Table;\n" +
                "    csvw:dialect [\n" +
                "        a csvw:Dialect;\n" +
                "        csvw:delimiter \";\";\n" +
                "        csvw:encoding \"UTF-8\";\n" +
                "        csvw:header \"1\"^^xsd:boolean;\n" +
                "        csvw:headerRowCount \"1\"^^xsd:nonNegativeInteger;\n" +
                "        csvw:trim \"1\"^^xsd:boolean;\n" +
                "    ] ];\n" +
                "    rml:referenceFormulation ql:CSV\n" +
                "    ];\n" +
                "" +
                "    rr:subjectMap [\n" +
                "        rr:template \"http://www.example.com/note/{@id}\";\n" +
                "        rr:class mail:note;\n" +
                "    ].\n";
    }

    @Override
    protected String expectedMapping(String filename) {
        return  "@prefix rr: <http://www.w3.org/ns/r2rml#>.\n" +
                "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
                "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
                "@prefix mail: <http://example.com/mail#>.\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
                "@prefix ex: <http://www.example.com/> .\n" +
                "@base <http://example.com/base> .\n" +
                "@prefix csvw: <http://www.w3.org/ns/csvw#>." +
                "" +
                "<#FromToMaps>\n" +
                "    rml:logicalSource [\n" +
                "    rml:source \"" + filename + "\";\n" +
                "    rml:source [\n" +
                "    a csvw:Table;\n" +
                "    csvw:dialect [\n" +
                "        a csvw:Dialect;\n" +
                "        csvw:delimiter \";\";\n" +
                "        csvw:encoding \"UTF-8\";\n" +
                "        csvw:header \"1\"^^xsd:boolean;\n" +
                "        csvw:headerRowCount \"1\"^^xsd:nonNegativeInteger;\n" +
                "        csvw:trim \"1\"^^xsd:boolean;\n" +
                "    ] ];\n" +
                "    rml:referenceFormulation ql:CSV\n" +
                "    ];\n" +
                "" +
                "    rr:subjectMap [\n" +
                "        rr:template \"http://www.example.com/note/{@id}\";\n" +
                "        rr:class mail:note;\n" +
                "    ].\n";
    }
}