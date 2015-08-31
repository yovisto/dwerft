package de.dwerft.lpdc.importer.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import de.dwerft.lpdc.importer.general.MappingDefinition.ContentSource;
import de.dwerft.lpdc.importer.general.MappingDefinition.TargetPropertyType;

/**
 * The mapper is responsible for loading and managing mapping definitions and
 * deciding which mappings have to executed based on a given input XML node.
 * 
 * @author hagt
 *
 */
public class Mapper {
	
	/**
	 * All mapping definitions
	 */
	private Set<MappingDefinition> mappings;

	/**
	 * Creates a new mapper.
	 * 
	 * @param mappings The mapping definitions
	 */
	public Mapper(Set<MappingDefinition> mappings) {
		this.mappings = mappings;
	}
	
	/**
	 * Creates a new mapping and loads mapping definitions from the mappings filename
	 * 
	 * @param mappingsFilename File containing mapping definitions
	 */
	public Mapper(String mappingsFilename) {
		
		mappings = new HashSet<MappingDefinition>();
		
		File mapFile = new File(mappingsFilename);
		Properties prop = new Properties();
		
		try {
			InputStream is = new FileInputStream(mapFile);
		
			if (is != null) {
				prop.load(is);
			}
			
			int counter = 1;
			MappingDefinition def;
			
			while ((def = buildMapping(prop, counter)) != null) {
				mappings.add(def);
				counter++;
			}
			
			is.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(mappings);
		
	}
	
	/**
	 * Creates a new MappingDefinition based on the property values in the
	 * properties and a certain index.
	 * 
	 * @param prop
	 * @param index
	 * @return
	 */
	private MappingDefinition buildMapping(Properties prop, int index) {
		
		String xmlNodePath = prop.getProperty("map"+index+".xmlNodePath");
		String conditionalAttributeName = prop.getProperty("map"+index+".conditionalAttributeName");
		String conditionalAttributeValue = prop.getProperty("map"+index+".conditionalAttributeValue");
		String contentSource = prop.getProperty("map"+index+".contentSource");
		String contentElementName = prop.getProperty("map"+index+".contentElementName");
		String targetOntologyClass = prop.getProperty("map"+index+".targetOntologyClass");
		String targetOntologyProperty = prop.getProperty("map"+index+".targetOntologyProperty");
		String targetPropertyType = prop.getProperty("map"+index+".targetPropertyType");
		
		if ("".equals(xmlNodePath)) xmlNodePath = null;
		if ("".equals(conditionalAttributeName)) conditionalAttributeName = null;
		if ("".equals(conditionalAttributeValue)) conditionalAttributeValue = null;
		if ("".equals(contentSource)) contentSource = null;
		if ("".equals(contentElementName)) contentElementName = null;
		if ("".equals(targetOntologyClass)) targetOntologyClass = null;
		if ("".equals(targetOntologyProperty)) targetOntologyProperty = null;
		if ("".equals(targetPropertyType)) targetPropertyType = null;
		
		if (xmlNodePath == null || targetOntologyClass == null) {
			System.err.println("WARNING: Mapping "+index+" does not have a xmlNodePath or targetOntologyClass.");
			return null;
		}
		
		if (targetOntologyProperty != null && targetPropertyType == null) {
			System.err.println("WARNING: Mapping "+index+" does not have a targetPropertyType.");
			return null;
		}
		
		ContentSource cs = null;
		if (contentSource != null) {
			cs = ContentSource.valueOf(contentSource);
		}
		TargetPropertyType tp = null;
		if (targetPropertyType != null) {
			tp = TargetPropertyType.valueOf(targetPropertyType);
		}
		
		return new MappingDefinition(
				xmlNodePath, 
				conditionalAttributeName,
				conditionalAttributeValue,
				cs,
				contentElementName,
				targetOntologyClass,
				targetOntologyProperty,
				tp
				);
	}
	
	
	private List<MappingDefinition> checkAttributeConditions(Iterator<MappingDefinition> iterator, Node node) {
		List<MappingDefinition> result = new ArrayList<MappingDefinition>();
		
		while (iterator.hasNext()) {
			MappingDefinition md = (MappingDefinition) iterator.next();
			boolean attributeCondition = true;
			Node attrNode = null;
			
			if (md.getConditionalAttributeName() != null && !"".equals(md.getConditionalAttributeName())) {
				attrNode = node.getAttributes().getNamedItem(md.getConditionalAttributeName());
				if (attrNode == null) {
					attributeCondition = false;
				}
			}
			
			if (md.getConditionalAttributeValue() != null && !"".equals(md.getConditionalAttributeValue())) {
				if (attrNode == null || !attrNode.getNodeValue().equals(md.getConditionalAttributeValue())) {
					attributeCondition = false;
				}
			}
			
			if (attributeCondition) {
				result.add(md);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns all mappings that have to evaluated for an input XML node.
	 * The result is ordered and contains at first mappings to create new resources
	 * and afterwards all mappings regarding properties.
	 * 
	 * @param node XML node to be mapped
	 * @return List of mappings.
	 */
	public List<MappingDefinition> getMappingsForNode(Node node) {
		List<MappingDefinition> result = new ArrayList<MappingDefinition>();
		
		String xmlPath = XMLProcessor.getXmlPath(node);
		
		// First check mappings that target a class
		Stream<MappingDefinition> classFilter = mappings.stream().filter(
				m -> xmlPath.equals(m.getXmlNodePath()) && m.getTargetOntologyProperty() == null);
		Iterator<MappingDefinition> classIterator = classFilter.iterator();
		List<MappingDefinition> classMappings = checkAttributeConditions(classIterator, node);

		// Then check mappings that target properties
		Stream<MappingDefinition> propFilter = mappings.stream().filter(
				m -> xmlPath.equals(m.getXmlNodePath()) && m.getTargetOntologyProperty() != null);
		Iterator<MappingDefinition> propIterator = propFilter.iterator();
		List<MappingDefinition> propMappings = checkAttributeConditions(propIterator, node);

		result.addAll(classMappings);
		result.addAll(propMappings);	
		
		return result;
	}

}
