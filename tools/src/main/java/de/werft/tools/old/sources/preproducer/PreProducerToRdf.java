package de.werft.tools.old.sources.preproducer;

import de.werft.tools.old.sources.general.AbstractXMLtoRDFconverter;
import de.werft.tools.old.sources.general.XMLProcessor;
import de.werft.tools.old.sources.Source;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Actual xml to rdf implementation for preproducer.
 * {@link AbstractXMLtoRDFconverter}
 *
 */
public class PreProducerToRdf extends AbstractXMLtoRDFconverter {

    private Source source;

	public PreProducerToRdf(InputStream ontologyFileName, String ontologyFormat,
			String mappingsFilename, Source source) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
        this.source = source;
	}

	public ArrayList<String> getAPIMethodOrder() {
		ArrayList<String> result = new ArrayList<>();

		result.add("info");
		result.add("listCharacters");
		result.add("listCrew");
		result.add("listDecorations");
		result.add("listExtras");
		result.add("listFigures");
		result.add("listScenes");
		result.add("listSchedule");
		return result;
	}

    @Override
    public void convert(String input) throws IOException {
        for (String method : getAPIMethodOrder()) {
            super.convert(method);
        }
    }

    @Override
    protected XMLProcessor getInputProcessor(String input) {
        return new XMLProcessor(source.get(input));
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
