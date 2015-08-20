package de.dwerft.lpdc.importer.general;

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
		return "MappingDefinition [xmlNodePath=" + xmlNodePath
				+ ", conditionalAttributeName=" + conditionalAttributeName
				+ ", conditionalAttributeValue=" + conditionalAttributeValue
				+ ", contentSource=" + contentSource + ", contentElementName="
				+ contentElementName + ", targetOntologyClass="
				+ targetOntologyClass + ", targetOntologyProperty="
				+ targetOntologyProperty + ", targetPropertyType="
				+ targetPropertyType + "]";
	}

}
