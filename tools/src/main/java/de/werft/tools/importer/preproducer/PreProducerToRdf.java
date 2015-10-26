package de.werft.tools.importer.preproducer;

import de.werft.tools.importer.general.XMLProcessor;
import de.werft.tools.importer.general.AbstractXMLtoRDFconverter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import java.io.InputStream;

/**
 * Actual xml to rdf implementation for preproducer.
 * {@link AbstractXMLtoRDFconverter}
 *
 */
public class PreProducerToRdf extends AbstractXMLtoRDFconverter {

	public PreProducerToRdf(InputStream ontologyFileName, String ontologyFormat,
			String mappingsFilename) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
	}

    public PreProducerToRdf(InputStream ontologyFile, String ontologyFormat, InputStream prpMapping) {
        super(ontologyFile, ontologyFormat, prpMapping);
    }

    /**
	 * Retrieves the identifier of the ScriptDocument element
	 * 
	 * @return Project identifier
	 */
	private String findProjectId() {
		Element documentElement = xmlProc.getDocumentElement();
		
		NodeList elementsByTagName = documentElement.getElementsByTagName("prp:project");
		Node item = elementsByTagName.item(0);

		String projectId = XMLProcessor.getValueOfAttribute(item, "projectid");

		if (projectId != null)
			return projectId;
		else
			return "";
	}

	@Override
	public void processingBeforeConvert() {
		// try to find the given project
        String pid = findProjectId();
		if (pid != null) {
			rdfProc.setUriIdentifierPrefix("Project/"+pid+"/");
		}
	}

	@Override
	public void processingAfterConvert() {
		// append the generated graph to the DWERFT model
		Model generatedModel = rdfProc.getGeneratedModel();
		Resource dwerft = generatedModel.getResource("http://filmontology.org/resource/DWERFT");

		String projectId = findProjectId();
		Resource project = rdfProc.getIdResourceMapping().get(projectId);

		Property property = generatedModel.getProperty("http://purl.org/dc/terms/hasPart");
		dwerft.addProperty(property, project);
	}
}
