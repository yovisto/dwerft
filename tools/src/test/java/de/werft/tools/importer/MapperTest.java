package de.werft.tools.importer;

import de.werft.tools.importer.general.Mapper;
import de.werft.tools.importer.general.MappingDefinition;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class MapperTest {

    //Define values for test mapping
    private final String xmlNodePath = "test_map.xmlNodePath=/my/path/to/project";
    private final String conditionalAttributeName = "test_map.conditionalAttributeName=test_attribute";
    private final String conditionalAttributeValue = "test_map.conditionalAttributeValue=0";
    private final MappingDefinition.ContentSource contentSource = MappingDefinition.ContentSource.ATTRIBUTE;
    private final String contentElementName = "test_map.contentElementName=test_name";
    private final String targetOntologyClass = "test_map.targetOntologyClass=test_class";
    private final String targetOntologyProperty = "test_map.targetOntologyProperty=test_property";
    private final MappingDefinition.TargetPropertyType targetPropertyType = MappingDefinition.TargetPropertyType.OBJECT_PROPERTY;

    @Test
    public void testStringMapper() {

        InputStream is = new ByteArrayInputStream(getSampleMapping().getBytes());
        Mapper mapper = new Mapper(is);

        Set<MappingDefinition> mappings = mapper.getMappings();

        //Set should only contain one mapping
        assertThat(mappings.size(), is(1));

        MappingDefinition md = null;

        for (MappingDefinition aMd : mappings) {
            md = aMd;
        }

        //Check all test values against expected values
        assertThat(md.getXmlNodePath(), is(xmlNodePath));
        assertThat(md.getConditionalAttributeName(), is(conditionalAttributeName));
        assertThat(md.getConditionalAttributeValue(), is(conditionalAttributeValue));
        assertThat(md.getContentSource(), is(contentSource));
        assertThat(md.getContentElementName(), is(contentElementName));
        assertThat(md.getTargetOntologyClass(), is(targetOntologyClass));
        assertThat(md.getTargetOntologyProperty(), is(targetOntologyProperty));
        assertThat(md.getTargetPropertyType(), is(targetPropertyType));
    }

    @Test
    public void testFileMapper() {

    }

    private String getSampleMapping() {

        StringBuilder sb = new StringBuilder();

        sb.append(xmlNodePath + " \n");
        sb.append(conditionalAttributeName + " \n");
        sb.append(conditionalAttributeValue + " \n");
        sb.append("test_map.contentSource=" + contentSource.toString() + " \n");
        sb.append(contentElementName + " \n");
        sb.append(targetOntologyClass + " \n");
        sb.append(targetOntologyProperty + " \n");
        sb.append("test_map.targetPropertyType=" + targetPropertyType.toString() + " \n");

        System.out.println(sb.toString());

        return sb.toString();
    }
}
