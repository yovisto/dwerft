package de.dwerft.lpdc.importer.dramaqueen;

import java.util.Map;

import org.w3c.dom.Element;

import de.dwerft.lpdc.importer.general.XMLProcessor;
import de.dwerft.lpdc.importer.general.XMLtoRDFconverter;

public class DramaqueenToRdf extends XMLtoRDFconverter {
	
	public DramaqueenToRdf(String ontologyFileName, String ontologyFormat, String mappingsFilename) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
	}
	
	private String findProjectId() {
		
		String result = null;
		
		Element documentElement = xmlProc.getDocumentElement();
		
		if ("ScriptDocument".equals(documentElement.getNodeName())) {
			String id = XMLProcessor.getValueOfAttribute(documentElement, "id");
			result = id;			
		}
		
		return result;
		
	}
	
	private void buildAttributeValueMappings() {
		Map<String, String> attributeValueMappings = rdfProc.getAttributeValueMappings();
		attributeValueMappings.put("interiorExterior_0", "int");
		attributeValueMappings.put("interiorExterior_1", "ext");
		attributeValueMappings.put("interiorExterior_2", "intext");
		attributeValueMappings.put("interiorExterior_3", "extint");
		
		attributeValueMappings.put("sex_0", "männlich");
		attributeValueMappings.put("sex_1", "weiblich");
		
	}

	@Override
	public void processingBeforeConvert() {
		String pid = findProjectId();
		if (pid != null) {
			rdfProc.setUriIdentifierPrefix("Project/"+pid+"/");
		}
		
		buildAttributeValueMappings();
	}


	@Override
	public void processingAfterConvert() {
		// TODO Auto-generated method stub
		
	}

}
