package de.dwerft.lpdc.importer.general;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.w3c.dom.Node;

/**
 * The mapper is responsible for deciding which mappings have to executed based on a given input XML node.
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
