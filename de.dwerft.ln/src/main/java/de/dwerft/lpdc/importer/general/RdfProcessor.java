package de.dwerft.lpdc.importer.general;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.MappingDefinition.ContentSource;
import de.dwerft.lpdc.importer.general.MappingDefinition.TargetPropertyType;

/**
 * The RdfProcessor is responsible for creating the appropriate RDF statements
 * based on the input XML nodes and a set of mappings.
 * 
 * @author hagt
 *
 */
public class RdfProcessor {
	
	/**
	 * Connector to the ontology model
	 */
	private OntologyConnector ontologyConnector;
	
	/**
	 * The new generated RDF model
	 */
	private Model generatedModel;
	
	/**
	 * Whenever a new resource is created, the ID of the XML element is stored
	 * together with the created resource.
	 */
	private Map<String, Resource> idResourceMapping;

	/**
	 * Whenever a new resource is created, the XML node is stored
	 * together with the created resource.
	 */
	private Map<Node, Resource> nodeResourceMapping;
	
	/**
	 * The stack always has the latest created resource on top in order to be
	 * able to do datatype property and object property linking.
	 */
	private Stack<Resource> resourceStack;
	
	/**
	 * The URI identifier prefix will be added for newly created resources
	 * between the general knowledge base prefix and the generated URI suffix.
	 * E.g. http://filmontology.org/resource/<uriIdentifierPrefix>/Scene/c900a8ce-e29b-41e5-b0cf-21bdbe6f11a1
	 */
	private String uriIdentifierPrefix = "";
	
	
	/**
	 * Predefined mappings for attribute values that will be evaluated before
	 * creating literals. The keys of the mappings must be in the format
	 * <property name>_<value>.
	 */
	private Map<String, String> attributeValueMappings;
	
	public RdfProcessor(OntologyConnector ontologyConnector) {
		this.ontologyConnector = ontologyConnector;
		generatedModel = ModelFactory.createDefaultModel();
		generatedModel.setNsPrefixes(this.ontologyConnector.getOntologyModel().getNsPrefixMap());
		
		idResourceMapping = new HashMap<String, Resource>();
		nodeResourceMapping = new HashMap<Node, Resource>();
		resourceStack = new Stack<Resource>();
		attributeValueMappings = new HashMap<String, String>();
	}

	/**
	 * Returns the generated RDF model
	 * @return The generated RDF model
	 */
	public Model getGeneratedModel() {
		return generatedModel;
	}
	
	
	/**
	 * Since the values of xml tags are only read as Strings, the real
	 * type must be determined in order to use the correct rdf data types
	 * This methods does so.
	 * e.g.: A value of "true" would return a Boolean true
	 * 
	 * Supported datatypes:
	 * Boolean, Integer, Float, String
	 * 
	 * @param value the String containing a value
	 * @return an Object of the correct type
	 */
	private static Object convertStringToAppropriateObject(String value) {
		Object o = value;
		
		//Boolean
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			o = Boolean.parseBoolean(value);
		}
		//Integer
		try {
			if (value.matches("-?[0-9]+")) {
				o = Integer.parseInt(value);
			}
		} catch (NumberFormatException nfe) { }
				
		//Float
		if (value.matches("-?[0-9]\\.[0-9]+")) {
			o = Float.parseFloat(value);
		}
		
		//If the input is neither numeric nor Boolean it is returned as a String
		return o;
	}
	
	private String getIdentifierOfNode(Node node) {
		
		String idValue = XMLProcessor.getValueOfAttribute(node, "id");

		if (idValue == null) {
			
			// try to find another attribute that contains id / identifier
			//TODO not very robust
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node item = attributes.item(i);
				if (item.getNodeName().toLowerCase().contains("id") ||
						item.getNodeName().toLowerCase().contains("identifier") ||
						item.getNodeName().toLowerCase().contains("rel")
						) {
					idValue = item.getNodeValue();
					break;
				}
			}
			
		}
		
		return idValue;
		
	}
	
	public String generateURI(OntClass ontologyClass, String nodeId) {
		
		if (uriIdentifierPrefix.contains(nodeId)) {
			return OntologyConstants.RESOURCE_NAMESPACE+ontologyClass.getLocalName()+"/"+nodeId;
		} else {
			return OntologyConstants.RESOURCE_NAMESPACE+uriIdentifierPrefix+ontologyClass.getLocalName()+"/"+nodeId;
		}
	}
	
	public Resource createOntologyClassInstance(Node node, MappingDefinition mapping) {
		Resource result = null;
		
		OntClass ontologyClass = ontologyConnector.getOntologyClass(mapping.getTargetOntologyClass());
		
		if (ontologyClass != null) {
		
			String nodeId = getIdentifierOfNode(node);
			
			if (nodeId == null) {
				nodeId = UUID.randomUUID().toString();
			}
			
			String uri = generateURI(ontologyClass, nodeId);
			Resource createdResource = generatedModel.createResource(uri);
			idResourceMapping.put(nodeId, createdResource);
			nodeResourceMapping.put(node, createdResource);
			resourceStack.push(createdResource);			
			
			Property typeProp = ontologyConnector.getProperty("rdf","type");
			createdResource.addProperty(typeProp, ontologyClass);
			
			result = createdResource;
		} else {
			throw new IllegalStateException("Ontology class not found in the ontology model: "+mapping.getTargetOntologyClass()+" -- "+node+" -- "+mapping);
		}

		return result;
	}
	
	public void linkDatatypeProperty(Node node, MappingDefinition mapping) {
		
		String value = null;
		
		if (mapping.getContentSource().equals(ContentSource.ATTRIBUTE)) {
			value = XMLProcessor.getValueOfAttribute(node, mapping.getContentElementName());
		} else 
		if (mapping.getContentSource().equals(ContentSource.TEXT_CONTENT)) {
			value = node.getTextContent();
		} else {
			throw new IllegalStateException("Datatype properties can only be filled from attribute values or text content: "+node+" -- "+mapping);
		}
		
		if (value == null) {
			throw new IllegalStateException("Value for datatype property linking could not be found: "+node+" -- "+mapping);
		} else {
			DatatypeProperty datatypeProperty = ontologyConnector.getOntologyDatatypeProperty(mapping.getTargetOntologyProperty());
			Resource latestResource = resourceStack.peek();
			String mappedValue = attributeValueMappings.get(datatypeProperty.getLocalName()+"_"+value);
			if (mappedValue != null) {
				value = mappedValue;
			}
			latestResource.addLiteral(datatypeProperty, convertStringToAppropriateObject(value));
		}

	}

	public void linkObjectProperty(Node node, MappingDefinition mapping) {
		
		if (mapping.getContentSource().equals(ContentSource.CONTAINMENT)) {
			Node containmentNode = node.getParentNode();
			while (containmentNode != null && !containmentNode.getNodeName().equals(mapping.getContentElementName())) {
				containmentNode = containmentNode.getParentNode();
			}
			if (containmentNode == null) {
				throw new IllegalStateException("Specified parent not found: "+mapping.getContentElementName()+" -- "+node+" -- "+mapping);
			} else {
				
				Resource containmentResource = nodeResourceMapping.get(containmentNode);
				if (containmentResource != null) {
					ObjectProperty objectProperty = ontologyConnector.getOntologyObjectProperty(mapping.getTargetOntologyProperty());
					Resource latestResource = resourceStack.peek();
					containmentResource.addProperty(objectProperty, latestResource);
				}
			}
		} else 
		if (mapping.getContentSource().equals(ContentSource.REFERENCE)) {
			String referenceValue = XMLProcessor.getValueOfAttribute(node, mapping.getContentElementName());
			
			Resource resourceToLink = idResourceMapping.get(referenceValue);
			Resource latestResource = resourceStack.peek();
			ObjectProperty objectProperty = ontologyConnector.getOntologyObjectProperty(mapping.getTargetOntologyProperty());
			
			if (resourceToLink != null) {
				latestResource.addProperty(objectProperty, resourceToLink);
			} else {
				throw new IllegalStateException("Referenced resource could not be found: "+referenceValue+" -- "+node+" -- "+mapping);
			}
		}
	}
	
	public Resource createRDF(Node node, MappingDefinition mapping) {
		Resource result = null;
		
		if (mapping.getContentSource() == null && mapping.getTargetOntologyProperty() == null) {
			createOntologyClassInstance(node, mapping);
		} else 
		if (mapping.getTargetOntologyProperty() != null && mapping.getTargetPropertyType().equals(TargetPropertyType.DATATYPE_PROPERTY)) {
			linkDatatypeProperty(node, mapping);
		} else 
		if (mapping.getTargetOntologyProperty() != null && mapping.getTargetPropertyType().equals(TargetPropertyType.OBJECT_PROPERTY)) {
			linkObjectProperty(node, mapping);
		}
		
		return result;
	}

	public String getUriIdentifierPrefix() {
		return uriIdentifierPrefix;
	}

	public void setUriIdentifierPrefix(String uriIdentifierPrefix) {
		this.uriIdentifierPrefix = uriIdentifierPrefix;
	}

	public Map<String, String> getAttributeValueMappings() {
		return attributeValueMappings;
	}

	public void setAttributeValueMappings(Map<String, String> attributeValueMappings) {
		this.attributeValueMappings = attributeValueMappings;
	}

}
