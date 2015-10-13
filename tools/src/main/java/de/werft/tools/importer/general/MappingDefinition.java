package de.werft.tools.importer.general;


/**
 * A MappingDefinition exactly defines how a particular XML element will be
 * converted to a particular RDF element.
 * 
 * In general, there are three types of mappings:
 * - An XML tag will result in the creation of a new instance of an ontology class (a new individual)
 * - An XML attribute value or XML text content value will result in the creation of a new property value belonging to a certain individual
 * - An XML containment hierarchy or XML attribute value with a reference will result in the creation of a new object property linking
 * 
 * A MappingDefinition must have at least a xmlNodePath and a targetOntologyClass.
 * 
 * @author hagt
 *
 */
public class MappingDefinition {
	
	public MappingDefinition(String xmlNodePath,
			String conditionalAttributeName, String conditionalAttributeValue,
			ContentSource contentSource, String contentElementName,
			String targetOntologyClass, String targetOntologyProperty,
			TargetPropertyType targetPropertyType) {
		super();
		this.xmlNodePath = xmlNodePath;
		this.conditionalAttributeName = conditionalAttributeName;
		this.conditionalAttributeValue = conditionalAttributeValue;
		this.contentSource = contentSource;
		this.contentElementName = contentElementName;
		this.targetOntologyClass = targetOntologyClass;
		this.targetOntologyProperty = targetOntologyProperty;
		this.targetPropertyType = targetPropertyType;
	}

	public enum ContentSource {
		
		/**
		 * The content will be extracted from the specific attribute.
		 * Mainly used for datatype properties.
		 */
		ATTRIBUTE,
		
		/**
		 * The content will be extracted from XML text content.
		 * Mainly used for datatype properties.
		 */
		TEXT_CONTENT,
		
		
		/**
		 * The content of the mapping will be determined by a surrounding XML parent node.
		 * Mainly used for object properties.
		 */
		CONTAINMENT,
		
		
		/**
		 * The content of the mapping will be determined by a reference in an attribute.
		 * Mainly used for object properties.
		 */
		REFERENCE
	}
	
	public enum TargetPropertyType {
		
		/**
		 * The target property is an object property.
		 */
		OBJECT_PROPERTY,
		
		/**
		 * The target property is a datatype property.
		 */
		DATATYPE_PROPERTY
		
	}
	
	/**
	 * Complete path to the XML node that will be mapped. Elements will be
	 * written with namespaces but without angle brackets. 
	 * Example: /ScriptDocument/characters/Character/Property
	 */
	private String xmlNodePath;
	
	/**
	 *  Optional condition: the XML node additionally must have a certain attribute.
	 *  If not required, use null.
	 */
	private String conditionalAttributeName;

	/**
	 * Optional condition: the XML node additionally must have a certain attribute value.
	 * If not required, use null.
	 */
	private String conditionalAttributeValue;

	/**
	 * From where the content will extracted
	 */
	private ContentSource contentSource;
	
	/**
	 * If content will be extracted from an attribute, this is the name of the attribute.
	 * If a surrounding containment will be extracted, this is the name of the parent xml tag.
	 */
	private String contentElementName;
	
	/**
	 * Full URI of the ontology class to be used for the mapping.
	 */
	private String targetOntologyClass;
	
	/**
	 * Full URI of the ontology property that will be used to link the literal
	 * value (datatype properties) or other resources (object properties).
	 */
	private String targetOntologyProperty;
	
	/**
	 * Type of the ontology property, either object property or datatype property.
	 */
	private TargetPropertyType targetPropertyType;

	public String getXmlNodePath() {
		return xmlNodePath;
	}

	public String getConditionalAttributeName() {
		return conditionalAttributeName;
	}

	public String getConditionalAttributeValue() {
		return conditionalAttributeValue;
	}

	public ContentSource getContentSource() {
		return contentSource;
	}

	public String getContentElementName() {
		return contentElementName;
	}

	public String getTargetOntologyClass() {
		return targetOntologyClass;
	}

	public String getTargetOntologyProperty() {
		return targetOntologyProperty;
	}

	public TargetPropertyType getTargetPropertyType() {
		return targetPropertyType;
	}

	@Override
	public String toString() {
		
		return "map.xmlNodePath=" + xmlNodePath + "\n"
				+ "map.conditionalAttributeName=" + conditionalAttributeName + "\n"
				+ "map.conditionalAttributeValue=" + conditionalAttributeValue + "\n"
				+ "map.contentSource=" + contentSource  + "\n"
				+ "map.contentElementName="	+ contentElementName  + "\n"
				+ "map.targetOntologyClass=" + targetOntologyClass  + "\n"
				+ "map.targetOntologyProperty=" + targetOntologyProperty + "\n"
				+ "map.targetPropertyType=" + targetPropertyType;
	}

}
