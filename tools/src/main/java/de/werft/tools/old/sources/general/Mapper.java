package de.werft.tools.old.sources.general;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * The mapper is responsible for loading and managing mapping definitions and
 * deciding which mappings have to executed based on a given input XML node.
 * 
 * @author hagt
 *
 */
public class Mapper {
	
	/** The Logger. */
	private static Logger L = LogManager.getLogger();
	
	/**
	 * All mapping definitions
	 */
	private Set<MappingDefinition> mappings;

	/**
	 * Creates a new Mapper and loads mapping definitions from the mappings filename
	 * 
	 * @param mappingsFilename File containing mapping definitions
	 */
	public Mapper(String mappingsFilename) {
        System.out.println(mappingsFilename);
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
			L.info("Mapping with file " + mappingsFilename + " successful");
			
		} catch (IOException e) {
			L.warn("Encountered error while mapping with file " + mappingsFilename + ": " + e);
		}
		
	}

    public Mapper(InputStream mapping) {
        mappings = new HashSet<MappingDefinition>();

        Properties prop = new Properties();
        try {
            prop.load(mapping);
            int counter = 1;
            MappingDefinition def;

            while ((def = buildMapping(prop, counter)) != null) {
                mappings.add(def);
                counter++;
            }

            L.info("Mapping with file " + mapping.toString() + " successful");
            mapping.close();
        } catch (IOException e) {
            L.warn("Encountered error while reading with file " + mapping.toString() + ": " + e);
        }

    }
	
	/**
	 * Creates a new MappingDefinition based on the property values in the
	 * properties and a certain index.
	 * 
	 * @param prop The complete set of properties
	 * @param index The number of the mapping definition (the nth mapping)
	 * @return A new MappingDefinition created from the properties
	 */
	private MappingDefinition buildMapping(Properties prop, int index) {
		
		if (!prop.containsKey("map"+index+".xmlNodePath")) {
			return null;
		}
		
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
		
		MappingDefinition.ContentSource cs = null;
		if (contentSource != null) {
			cs = MappingDefinition.ContentSource.valueOf(contentSource);
		}
		MappingDefinition.TargetPropertyType tp = null;
		if (targetPropertyType != null) {
			tp = MappingDefinition.TargetPropertyType.valueOf(targetPropertyType);
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
		L.debug("Retrieving mappings for node " + node.getNodeName());
		
		List<MappingDefinition> result = new ArrayList<MappingDefinition>();
		
		String xmlPath = XMLProcessor.getXmlPath(node).toLowerCase();
		
		// First check mappings that target a class
		List<MappingDefinition> tempClassMappings = new ArrayList<MappingDefinition>();
		for (MappingDefinition m : mappings) {
			if (xmlPath.equals(m.getXmlNodePath().toLowerCase()) && m.getTargetOntologyProperty() == null) {
				tempClassMappings.add(m);
			}
		}
		Iterator<MappingDefinition> classIterator = tempClassMappings.iterator();
		List<MappingDefinition> classMappings = checkAttributeConditions(classIterator, node);
		
		// Then check mappings that target properties
		List<MappingDefinition> tempPropMappings = new ArrayList<MappingDefinition>();
		for (MappingDefinition m : mappings) {
			if (xmlPath.equals(m.getXmlNodePath().toLowerCase()) && m.getTargetOntologyProperty() != null) {
				tempPropMappings.add(m);
			}
		}
		Iterator<MappingDefinition> propIterator = tempPropMappings.iterator();
		List<MappingDefinition> propMappings = checkAttributeConditions(propIterator, node);
		
		result.addAll(classMappings);
		result.addAll(propMappings);	
		
		return result;
	}

	/**
	 * Get all mapping definitions
	 * @return
	 */
	public Set<MappingDefinition> getMappings() {
		return mappings;
	}

}