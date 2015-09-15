package de.dwerft.lpdc.importer.dramaqueen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.XMLProcessor;
import de.dwerft.lpdc.importer.general.XMLtoRDFconverter;

public class DramaqueenToRdf extends XMLtoRDFconverter {
	
	public DramaqueenToRdf(String ontologyFileName, String ontologyFormat, String mappingsFilename) {
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
		
		if ("ScriptDocument".equals(documentElement.getNodeName())) {
			String id = XMLProcessor.getValueOfAttribute(documentElement, "id");
			result = id;			
		}
		
		return result;
		
	}
	
	
	/**
	 * Searches for the archive and retrieves the identifier. The identifier
	 * is used to determine whether a scene is visible or not
	 * (if in the archive == not visible).
	 * 
	 * @return
	 * 				The archive identifier
	 */
	private String findArchiveId() {
		
		Element documentElement = xmlProc.getDocumentElement();
		
		NodeList nodeList = documentElement.getElementsByTagName("Plot");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node plot = nodeList.item(i);
			String plotId = XMLProcessor.getValueOfAttribute(plot, "id");
			NodeList childNodes = plot.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node item = childNodes.item(j);
				if ("Property".equals(item.getNodeName()) && "157".equals(XMLProcessor.getValueOfAttribute(item, "id"))) {
					return plotId;
				}
			}
		}
		return null;
	}
	
	private void buildAttributeValueMappings() {
		Map<String, String> attributeValueMappings = rdfProc.getAttributeValueMappings();
		attributeValueMappings.put("interiorExterior_0", "int");
		attributeValueMappings.put("interiorExterior_1", "ext");
		attributeValueMappings.put("interiorExterior_2", "intext");
		attributeValueMappings.put("interiorExterior_3", "extint");
		
		attributeValueMappings.put("sex_0", "männlich");
		attributeValueMappings.put("sex_1", "weiblich");
		
		Element documentElement = xmlProc.getDocumentElement();
		
		// Build attribute value id to value mappings for daytimes
		NodeList timeTemp = documentElement.getElementsByTagName("times_of_day");
		Node timeRoot = timeTemp.item(0);
		NodeList times = timeRoot.getChildNodes();
		for (int i = 0; i < times.getLength(); i++) {
			Node child = times.item(i);
			if (child.getNodeName().equals("TimeOfDay")) {
				String id = XMLProcessor.getValueOfAttribute(child,"id");
				NodeList props = child.getChildNodes();
				for (int j = 0; j < props.getLength(); j++) {
					Node prop = props.item(j);
					if (prop.getNodeName().equals("Property")) {
						if ("0".equals(XMLProcessor.getValueOfAttribute(prop, "id"))) {
							String timeName = XMLProcessor.getValueOfAttribute(prop,"value");
							attributeValueMappings.put("dayTime_"+id, timeName);
						}
					}
				}
			}
		}
	}

	@Override
	public void processingBeforeConvert() {
		String pid = findProjectId();
		if (pid != null) {
			rdfProc.setUriIdentifierPrefix("Project/"+pid+"/");
		}
		
		buildAttributeValueMappings();
	}

	
	private void buildSceneNumbering(Element root) {
		
		ArrayList<String> stepListInCorrectOrder = new ArrayList<String>();
		
		Element storyChildren = null;
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if ("children".equals(item.getNodeName())) {
				storyChildren = (Element) item;
				break;
			}
		}
		
		if (storyChildren == null) {
			return;
		}
		
		NodeList stations = storyChildren.getElementsByTagName("Station");
		
		for (int j = 0; j < stations.getLength(); j++) {				
			Element station = (Element)stations.item(j);				
			Element stationChildren = (Element)station.getElementsByTagName("children").item(0);
			if (stationChildren != null) {
				NodeList proxies = stationChildren.getElementsByTagName("Proxy");
				for (int k = 0; k < proxies.getLength(); k++) {
					Node proxy = proxies.item(k);
					String stepid = XMLProcessor.getValueOfAttribute(proxy, "proxy_for");
					stepListInCorrectOrder.add(stepid);
				}
			}
		}
		
		Map<String, Node> idNode = new HashMap<String, Node>();
		
		NodeList allSteps = root.getElementsByTagName("Step");
		for (int i = 0; i < allSteps.getLength(); i++) {
			Node step = allSteps.item(i);
			idNode.put(XMLProcessor.getValueOfAttribute(step, "id"), step);
		}
		
		ArrayList<String> allFrameIds = new ArrayList<String>();
		for (String stepId : stepListInCorrectOrder) {
			Node node = idNode.get(stepId);
			
			NodeList frames = ((Element)node).getElementsByTagName("Frame");
			for (int i = 0; i < frames.getLength(); i++) {
				Node frame = frames.item(i);
				String frameId = XMLProcessor.getValueOfAttribute(frame, "id");
				allFrameIds.add(frameId);
			}
		}
		
		int sceneNumber = 0;
		for (String frameId : allFrameIds) {
			sceneNumber++;
			rdfProc.linkDatatypeProperty(frameId, OntologyConstants.ONTOLOGY_NAMESPACE+"sceneNumber", Integer.toString(sceneNumber));
		}
	}
	 

	@Override
	public void processingAfterConvert() {
		
//		String archiveId = findArchiveId();
		
		buildSceneNumbering(xmlProc.getDocumentElement());
		
		Model generatedModel = rdfProc.getGeneratedModel();

		Resource dwerft = generatedModel.getResource("http://filmontology.org/resource/DWERFT");

		String projectId = findProjectId();
		Resource project = rdfProc.getIdResourceMapping().get(projectId);
		
		Property property = generatedModel.getProperty("http://purl.org/dc/terms/hasPart");
		
		dwerft.addProperty(property, project);
		
		
	}

}
