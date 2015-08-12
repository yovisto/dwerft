package de.dwerft.lpdc.importer.dramaqueen;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.Mapping;
import de.dwerft.lpdc.importer.general.Mapping.MappingAction;
import de.dwerft.lpdc.importer.general.RdfGenerator;

public class DramaQueenConverter extends RdfGenerator {
	
	/*
	 * A stack that always has the lastest created RDF resource on top
	 */
	private Stack<Resource> resourceStack = null;

	private HashSet<Mapping> mappings = null;
	
	private String projectIdentifier = null;
	
	/*
	 * Mappings of DramaQueen identifiers to created RDF resouces
	 */
	private Map<String, Resource> idResourceMapping = null;
	
	/*
	 * Mappings of IDs in attribute values to named attribute values 
	 */
	private Map<String, String> attributeValueMappings = null;

	public DramaQueenConverter(String owl, String format) {
		super(owl, format);
		initializeMappings();
		
		idResourceMapping = new HashMap<String, Resource>();
		resourceStack = new Stack<Resource>();	
	}
	
	
	private void initializeInternalDocumentData(Element documentElement) {
		
		Node node = null;
		
		if ("ScriptDocument".equals(documentElement.getNodeName())) {
			node = documentElement;
		} else {
			NodeList list = documentElement.getElementsByTagName("ScriptDocument");
			if (list.getLength() == 1) {
				node = list.item(0);
			} else {
				throw new IllegalStateException("No ScriptDocument id found");
			}
		}
		
		String pid = getValueOfAttribute(node, "id");
		if (pid != null && pid != "") {
			projectIdentifier = pid;
		} else {
			throw new IllegalStateException("No ScriptDocument id found");
		}
		
		attributeValueMappings = new HashMap<String, String>();
		attributeValueMappings.put("interiorExterior_0", "int");
		attributeValueMappings.put("interiorExterior_1", "ext");
		attributeValueMappings.put("interiorExterior_2", "intext");
		attributeValueMappings.put("interiorExterior_3", "extint");
		
		attributeValueMappings.put("sex_0", "männlich");
		attributeValueMappings.put("sex_1", "weiblich");
		
		// Build attribute value id to value mappings for daytimes
		NodeList timeTemp = documentElement.getElementsByTagName("times_of_day");
		Node timeRoot = timeTemp.item(0);
		NodeList times = timeRoot.getChildNodes();
		for (int i = 0; i < times.getLength(); i++) {
			Node child = times.item(i);
			if (child.getNodeName().equals("TimeOfDay")) {
				String id = getValueOfAttribute(child,"id");
				NodeList props = child.getChildNodes();
				for (int j = 0; j < props.getLength(); j++) {
					Node prop = props.item(j);
					if (prop.getNodeName().equals("Property")) {
						if ("0".equals(getValueOfAttribute(prop, "id"))) {
							String timeName = getValueOfAttribute(prop,"value");
							attributeValueMappings.put("dayTime_"+id, timeName);
						}
					}
				}
			}
		}
	}
	
	private void initializeMappings() {
		mappings = new HashSet<Mapping>();
		mappings.add(Mapping.createMapping("ScriptDocument", MappingAction.MAP, "Project"));
		mappings.add(Mapping.createMapping("Step", MappingAction.MAP, "SceneGroup"));
		mappings.add(Mapping.createMapping("Frame", MappingAction.MAP, "Scene"));
		mappings.add(Mapping.createMapping("Location", MappingAction.MAP, "Set"));
		mappings.add(Mapping.createMapping("Character", MappingAction.MAP, "Character"));
		mappings.add(Mapping.createMapping("sequences", MappingAction.MAP, "Episode"));

		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "0", "ScriptDocument", 1, "name"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "168", "Frame", 1, "interiorExterior"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "126", "Frame", 1, "dayTime"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "9", "Frame", 1, "sceneDescription"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "0", "Location", 1, "name"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "0", "Character", 1, "name"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "181", "Character", 1, "fullName"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "209", "Character", 1, "aliasName"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "117", "Character", 1, "sex"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "99", "Character", 1, "appearance"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "100", "Character", 1, "relationshipStatus"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "101", "Character", 1, "socialStatus"));
		mappings.add(Mapping.createMapping(
				MappingAction.CONTEXTMAP, "Property", "id", "102", "Character", 1, "occupation"));
		
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Character", null, null, "ScriptDocument", 2, "hasCharacter"));
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Location", null, null, "ScriptDocument", 2, "hasLocation"));
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Frame", null, null, "Step", 4, "hasScene"));
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Step", null, null, "sequences", 1, "hasSceneGroup"));
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Character", null, null, "ScriptDocument", 2, "hasCharacter"));
		mappings.add(Mapping.createMapping(
				MappingAction.LINK, "Property", "id", "10", "Frame", 1, "sceneSet"));
	}
	
	private Node getParentNode(Node node, int level) {
		Node temp = node;

		for (int i = 0; i < level; i++) {
			temp = temp.getParentNode();
			if (temp == null) {
				return null;
			}
		}
		
		return temp;
	}
	
	/**
	 * Helper method - extracts the value of a certain attribute of a node
	 * 
	 * @param node
	 * @param attrName
	 * @return
	 */
	public static String getValueOfAttribute(Node node, String attrName) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			Node attrNode = attributes.getNamedItem(attrName);
			if (attrNode != null) {
				return attrNode.getNodeValue();
			}
		}
		return null;
	}
	
	private ArrayList<Mapping> getMappingsForNode(Node node) {
		ArrayList<Mapping> result = new ArrayList<Mapping>();
		
		String nodeName = node.getNodeName();
				
		Stream<Mapping> filter = mappings.stream().filter(m -> m.getInput().equals(nodeName));
		Iterator<Mapping> iterator = filter.iterator();
		while(iterator.hasNext()) {
			Mapping mapping = iterator.next();

			boolean context = false;
			boolean attribute = false;
			
			if (mapping.getContext() != null && !"".equals(mapping.getContext())) {
				int distance = mapping.getDistance();
				if (distance == 0) distance = 1;
				Node parentNode = getParentNode(node, distance);
				context = mapping.getContext().equals(parentNode.getNodeName());
			} else {
				context = true;
			}
			
			if (mapping.getInputAttributeName() != null && !"".equals(mapping.getInputAttributeName()) &&
					mapping.getInputAttributeValue() != null && !"".equals(mapping.getInputAttributeValue())) {
				String attrValue = getValueOfAttribute(node, mapping.getInputAttributeName());
				attribute = mapping.getInputAttributeValue().equals(attrValue);
			} else {
				attribute = true;
			}
			
			if (context && attribute) {
				result.add(mapping);
			}
			
		}
		return result;
	}
	
	public Resource createOntologyClassInstance(OntClass ontClass, Node node, Mapping mapping) {
		Resource result = null;
		
		String dramaQueenId = getValueOfAttribute(node, "id");
		if (dramaQueenId == null) {
			dramaQueenId = "1";
		}
		
		String resourcePrefix = ontologyModel.getNsPrefixURI(OntologyConstants.RESOURCE_PREFIX);
		String newResourceURI = null;
		if("ScriptDocument".equals(node.getNodeName())) {
			newResourceURI = resourcePrefix+ontClass.getLocalName() + "/" + dramaQueenId;
		} else {
			newResourceURI = resourcePrefix+"Project/"+projectIdentifier+"/"+ontClass.getLocalName() + "/" + dramaQueenId;
		}
		
		result = generatedModel.createResource(newResourceURI);
		
		// Add rdf:type edge to class
		Property typeProp = getRdfProperty("rdf","type");
		result.addProperty(typeProp, ontClass);
		
		// Add dramaqueen id edge to class
		setProperty("identifierDramaQueen", dramaQueenId, result);

		// Remember ID and associated RDF resource
		idResourceMapping.put(dramaQueenId, result);
		
		// Put the recent created resource on the stack
		resourceStack.push(result);
		
		return result;
		
	}
	
	public void createObjectPropertyLinking(ObjectProperty objectProperty, Node node, Mapping mapping) {

		if (mapping.getAction().equals(MappingAction.LINK)) {
			
			if (mapping.getInputAttributeName() != null && !"".equals(mapping.getInputAttributeName()) &&
					mapping.getInputAttributeValue() != null && !"".equals(mapping.getInputAttributeValue())) {
				// In case the object to link is referenced by an identifier in a property node 
				
				String attributeValue = getValueOfAttribute(node, "value");
				if (attributeValue != null && attributeValue != "") {
					Resource resourceToLink = idResourceMapping.get(attributeValue);
					if (resourceToLink != null) {
						Resource peek = resourceStack.peek();
						peek.addProperty(objectProperty, resourceToLink);
					}
				}
			} else {
				
				// In case the object to link is referenced implicitly via containment
				Node parentNode = getParentNode(node, mapping.getDistance());
				if (parentNode != null) {
					String parentId = getValueOfAttribute(parentNode, "id");
					Resource parentResource = null;
					
					if (parentId == null) {
						parentResource = resourceStack.elementAt(resourceStack.size()-1-mapping.getDistance());
					} else {
						parentResource = idResourceMapping.get(parentId);
					}
					parentResource.addProperty(objectProperty, resourceStack.peek());
				}
			}
		}
	}

	public void createDatatypePropertyLinking(DatatypeProperty datatypeProperty, Node node, Mapping mapping) {
		
		String attributeValue = getValueOfAttribute(node, "value");
		if (attributeValue != null && attributeValue != "") {
			Resource peek = resourceStack.peek();

			// Check whether an attribute value is mapped
			String valMap = attributeValueMappings.get(mapping.getOutput()+"_"+attributeValue);
			if (valMap != null) {
				attributeValue = valMap;
			}
			setProperty(mapping.getOutput(), attributeValue, peek);
		}
	}

	public Resource createRDF(Node node, Mapping mapping) {
		Resource result = null;
		
		// In case the current node will be mapped to an ontology class
		Optional<OntClass> optClass = getOntologyClass(OntologyConstants.ONTOLOGY_PREFIX, mapping.getOutput());
		if (optClass.isPresent() && mapping.getAction().equals(MappingAction.MAP)) {
			result = createOntologyClassInstance(optClass.get(), node, mapping);
		} else {
			
			Optional<ObjectProperty> optObjProp = getOntologyObjectProperty(OntologyConstants.ONTOLOGY_PREFIX, mapping.getOutput());
			if (optObjProp.isPresent()) {
				createObjectPropertyLinking(optObjProp.get(), node, mapping);
			} else {
				Optional<DatatypeProperty> optDataProp = getOntologyDatatypeProperty(OntologyConstants.ONTOLOGY_PREFIX, mapping.getOutput());
				if (optDataProp.isPresent()) {
					createDatatypePropertyLinking(optDataProp.get(), node, mapping);
				} else {
					throw new IllegalStateException("No ontology element defined for mapping target: "+mapping.getOutput());
				}
			}
		}
		
		return result;		
	}
	
	public void traverse(Node node) {
		
		boolean newIndiviudalWasCreated = false;
		boolean traverseChildren = true;
		
		ArrayList<Mapping> mappings = getMappingsForNode(node);

		if (!mappings.isEmpty()) {
			Optional<Mapping> mapMapping = mappings.stream().filter(m -> m.getAction().equals(MappingAction.MAP)).findFirst();

			if (mapMapping.isPresent()) {
				Mapping mapping = mapMapping.get();
				Resource newResource = createRDF(node, mapping);
				if (newResource != null) {
					newIndiviudalWasCreated = true;
				}
				mappings.remove(mapping);
			}
			
			for (Mapping mapping : mappings) {
				createRDF(node, mapping);
			}
			
		}
		
		if (traverseChildren) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				traverse(child);
			}
		}
		
		if (newIndiviudalWasCreated) {
			resourceStack.pop();
		}
	}
	

	@Override
	public void generate(InputStream stream) {
		
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(stream);
			
			Element documentElement = doc.getDocumentElement();
			initializeInternalDocumentData(documentElement);
			traverse(documentElement);
			
//			initializeInternalData(documentElement);
//			
//			archiveId = findArchiveId(documentElement);
//			scriptDocumentId = getValueOfAttribute(documentElement, "id");		
//			
//			traverse(documentElement);
//			buildSceneNumbering(documentElement);
			
		} catch ( ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
