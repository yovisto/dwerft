package de.werft.tools.importer.preproducer;

import de.werft.tools.importer.general.XMLProcessor;
import de.werft.tools.importer.general.XMLtoRDFconverter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class PreProducerToRdf extends XMLtoRDFconverter {

	public PreProducerToRdf(String ontologyFileName, String ontologyFormat,
			String mappingsFilename) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
	}
	
	/**
	 * Retrieves the identifier of the ScriptDocument element
	 * 
	 * @return Project identifier
	 */
	private String findProjectId() {
		
		String result = null;
		
		Element documentElement = xmlProc.getDocumentElement();
		
		NodeList elementsByTagName = documentElement.getElementsByTagName("prp:project");
		Node item = elementsByTagName.item(0);
		
		result = XMLProcessor.getValueOfAttribute(item, "projectid");

		return result;
		
	}

	@Override
	public void processingBeforeConvert() {
		String pid = findProjectId();
		if (pid != null) {
			rdfProc.setUriIdentifierPrefix("Project/"+pid+"/");
		}
	}

	@Override
	public void processingAfterConvert() {
		
		Model generatedModel = rdfProc.getGeneratedModel();

		Resource dwerft = generatedModel.getResource("http://filmontology.org/resource/DWERFT");

		String projectId = findProjectId();
		Resource project = rdfProc.getIdResourceMapping().get(projectId);
		
		Property property = generatedModel.getProperty("http://purl.org/dc/terms/hasPart");
		
		dwerft.addProperty(property, project);

	}

}
