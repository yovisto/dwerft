package de.werft.tools.importer.preproducer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.werft.tools.importer.general.Mapping;
import de.werft.tools.importer.general.RdfGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.StAXStreamBuilder;
import org.jdom2.output.XMLOutputter;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This implementation provides a basic xml to rdf parser.
 * The parser tries to be as general as possible. To do that we need
 * some basic outlines and assumptions about the xml file.
 * <br><br>
 * First of all we consider that not the whole xml file is used.
 * So you can tell the parser how many nodes to drop from the start 
 * (not the xml header) and to flag nodes you don't want. 
 * Also you can ignore nodes (not leafs) with leaving the class maping empty 
 * (the supgraph is evaluated).<br> 
 * To flag a node provide a ignore list with the node names. You can't ignore top nodes.
 * <br><br>
 * Second we expect that the ontology names and the xml tags are not always the same
 * so we provide a mapping for resources and properties. See {@link Mapping} for more informations
 * how to use these special mappings. We provide simple, context aware direct and broadcast link as normal mappings.
 * <br><br>
 * Third all classes getting a new UUID from us. This UUID is only machine unique. 
 * To link classes together we use the attriubtes 'id', 'rel' and 'ref'.
 * <br><br>
 * Fourth We have the follwoing assumptions about an xml graph (file).
 * The leafs without attributes are common literals. <br>
 * The leafs with attributes are classes but can be interpreted as literals if there are no corresponding mapping (class).<br>
 * The nodes with child's are always classes.<br>
 * Attributes are always literals.
 */
public class PreProducerGenerator extends RdfGenerator {

	/** The nodes to drop. */
	private int nodesToDrop;
	
	private List<String> ignoreNodes;
	
	protected HashSet<Mapping> mapping;
	
	/**
	 * Instantiates a new preproducer generator.
	 *	@see PreProducerGenerator
	 *
	 * @param owl
	 *            the ontology
	 * @param format
	 *            the ontology format
	 * @param ignoreNodes
	 *            list of nodes to ignore with subgraphs
	 * @param mapping
	 *            see {@link Mapping} for detailed information
	 * @param nodesToDrop
	 *            the nodes to drop
	 */
	public PreProducerGenerator(String owl, String format, List<String> ignoreNodes, HashSet<Mapping> mapping, int nodesToDrop) {
		super(owl, format);
		this.mapping = mapping;
		this.nodesToDrop = nodesToDrop;
		this.ignoreNodes = ignoreNodes;
	}

	public void setNodesToDrop(int nodesToDrop) {
		this.nodesToDrop = nodesToDrop;
	}
	
	/* (non-Javadoc)
	 * @see xmlparsers.RdfGenerator#generate(java.io.BufferedInputStream)
	 */
	@Override
	public void generate(InputStream stream) {
		/* get a dom representation for reasoning about the xml graphs instead
		 * of taking named properties */
		try {
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
			StAXStreamBuilder builder = new StAXStreamBuilder();
			Document doc = builder.build(reader);
			
			/* drop specified amount of nodes 
			 * the idea is to drop all nodes until we have the first 
			 * node or nodes we want to translate into an rdf graph 
			 * based on our ontology */
			List<Element> nl = new ArrayList<Element>();
			dropNodes(nl, doc.getRootElement(), nodesToDrop);	
			
			/* from here we walk through all existing top nodes 
			 * and convert them */
			for (Element n : nl) {
				walkNode(n, null);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

	private void walkNode(Element node, Resource parentResource) {
		/* recursively walk through all nodes and convert them accordingly */
		/* identify if we ignore this node and the subgraph */
		if (isIgnored(node.getName())) {
			return;
		}
		
		/* take the parent nodes name */
		String parent = getParent(node, parentResource);
		String ontoName = evaluateContextMapAction(node.getName(), parent);
		
		/* check if we need to convert the subgraph to a string */
		Optional<Mapping> convertMapping = getConvertMapping(node.getName());
		if (convertMapping.isPresent()) {	
			/* create the property and return; at the moment we only support parentresources as property target */
			String subgraph = new XMLOutputter().outputString(node.getContent());
			setProperty(convertMapping.get().getOutput(), subgraph, parentResource);
			return;
		}
		
		
		/* if we have a common leaf we have a basic literal; the value has to be non null */
		if (node.getChildren().isEmpty() && !node.hasAttributes() && !node.getValue().isEmpty()) {
			/* we map the names and add the literal to his parent */
			setProperty(ontoName, node.getValue(), parentResource);
		}

		/* we have a leaf with attributes, they are interpreted as literals and the node as class,
		 * there is a possibility to take a leaf with attributes as literal property instead of a class */
		if (node.getChildren().isEmpty() && node.hasAttributes()) {
			if (!getOntologyClass(evaluateMapAction(node.getName())).isPresent()) {
				/* we have no corresponding class and interpret this leaf as literal */
				setProperty(ontoName, node.getValue(), parentResource);
				return;
			}
				
			Resource r = createClass(node, parent);
			
			/* we add all attributes as literals */
			createLiteralsFromAttributes(node, r);
			
			/* create the literal from the node value */
			if (!node.getValue().isEmpty()) {
				setProperty(ontoName, node.getValue(), r);
			}
			
			/* create the object property in the parent class aka backlink */
			/* the object property mapping can be null if we use the literals */
			createBacklinks(node, r, parentResource);
		}
		
		/* we have a common node this represents a owl class 
		 * we don't care if it is an anonymous node */
		if (!node.getChildren().isEmpty()) {
			/* we map the names and add the class to his parent */
			Resource r = createClass(node, parent);
			
			/* if we have a null resource we have an ignored node, but we use the children's with the parent resource */
			if (isNull(r)) {
				/* recursive call */
				for (Element e : node.getChildren()) {
					walkNode(e, parentResource);
				}
				return;
			}
			
			/* we add all attributes as literals */
			createLiteralsFromAttributes(node, r);
			
			/* create the object property in the parent class aka backlink */
			//setProperty(evaluateMapping("link", node.getName(), parent), r.getURI(), parentResource);
			createBacklinks(node, r, parentResource);
			
			/* recursive call */
			for (Element e : node.getChildren()) {
				walkNode(e, r);
			}
		}
	}

	/* returns the parent nodes name or an empty string */
	private String getParent(Element node, Resource parentResource) {
		if (!isNull(node.getParentElement()) && !isNull(parentResource)) {
			return node.getParentElement().getName();
		}
		return "";
	}

	/* create literals from attributes we assume only simple map actions */
	private void createLiteralsFromAttributes(Element node, Resource r) {
		Stream<Attribute> s = node.getAttributes().stream().filter(a -> !a.getValue().isEmpty());
		s.forEach(a -> setProperty(evaluateMapAction(a.getName()), a.getValue(), r));;
	}
	
	/* this method creates only named class and anon classes it returns null if we have no ont class */
	private Resource createClass(Element node, String parent) {
		Resource result = null;
		/* we need to search for an existing class with the identifier if we have one.
		 * as identifier we take id, rel, ref */
		if (hasIdAttribute(node)) {
			result = getExistingResource(node, parent);	
		}
		
		/* we have a common resource or null if we have no mapping */
		return !isNull(result) ? result : createFreshResource(node);
	}
	
	/* search for an existing resource with an specific id */
	private Resource getExistingResource(Element node, String parent) {
		Attribute a = getIdAttribute(node);
			
		/* get all resources with an id or rel property */
		Optional<DatatypeProperty> p = getOntologyDatatypeProperty(evaluateContextMapAction(a.getName(), parent));
		return getResourceThatSatisfiesLiteral(p.get(), a.getValue());
	}

	/* create a fresh resource from an element and set correct property */
	private Resource createFreshResource(Element node) {
		Optional<OntClass> o = getOntologyClass(evaluateMapAction(node.getName()));
		if (o.isPresent()) {
			
			/* use for as name prefix for all classes */
			String prefix = ontologyModel.getNsPrefixURI("for");
			String className = o.get().getLocalName();		
			
			return generatedModel.createResource(prefix + className + "/" + getUUID()).addProperty(getRdfProperty("rdf","type"), o.get()); 
		}
		return null;
	}
	
	/* we create the object links with the simple approach that we get all links as context nodes 
	 * and the output is used as object property for the ontology linking */
	private void createBacklinks(Element n, Resource r, Resource parent) {
		for (Mapping m : getMappings(n.getName(), Mapping.MappingAction.LINK)) {
			if (!isNull(parent) && StringUtils.containsIgnoreCase(parent.getNameSpace(), m.getLinkNode())) {
				/* be context aware */
				if (emptyOrCorrectContext(n, m)) {
					setProperty(m.getOutput(), r.getURI(), parent);		
				}
				
			} else {
				/* support for multiple backlinks, also to non direct parent nodes 
				 * if you use the direct back link option we override the default behavior
				 * we have a node that could be our wanted 
				 * now we search if the node is a direct parent */
				if (m.isDirectBackLink()) {
					setDirectBackLink(n, r, m);
				} else {	
					setMultiBackLink(n, r, m);
				}
			}
		}
	}

	private void setMultiBackLink(Element n, Resource r, Mapping m) {
		ResIterator itr = generatedModel.listSubjects();
		while (itr.hasNext()) {
			Resource linkNode = itr.next();
			if (StringUtils.containsIgnoreCase(linkNode.getNameSpace(), m.getLinkNode())) {
				if (emptyOrCorrectContext(n, m))  {
					/* be context aware */
					setProperty(m.getOutput(), r.getURI(), linkNode);		
				}
			}
		}
	}

	private void setDirectBackLink(Element n, Resource r, Mapping m) {
		Element parentNode = getDirectParent(n, m.getContext());
		if (!isNull(parentNode)) {
			/* we have direct parent and he gets our link */
			Resource parentResource = getExistingResource(parentNode, m.getContext());
			if (!isNull(parentResource)) {
				setProperty(m.getOutput(), r.getURI(), parentResource);
			}
		}
	}
	
	/* searches the direct parent for a node or null if we don't have one */
	private Element getDirectParent(Element n, String parentName) {
		if (n == null) {
			return null;
		}
		
		if (StringUtils.equalsIgnoreCase(n.getName(), parentName)) {
			return n;
		} else {
			return getDirectParent(n.getParentElement(), parentName);
		}
	}
	
	private Optional<Mapping> getConvertMapping(String input) {
		Optional<Mapping> o = getMappings(input, Mapping.MappingAction.CONVERT).stream().findFirst();
		return o;
	}
	
	
	
	/* evaluate context aware mappings and use simple mappings as fallback 
	 * for this we have a higher priority for context aware mappings over simple mappings */
	private String evaluateContextMapAction(String input, String context) {
		String name = input;
		Optional<Mapping> o = getMappings(input, Mapping.MappingAction.CONTEXTMAP).stream().filter(m -> StringUtils.equalsIgnoreCase(context, m.getContext())).findFirst();
		if (o.isPresent()) {
			name = o.get().getOutput();
		} else {
			name = evaluateMapAction(input);
		}
		
		return name;
	}

	/* we assume that there is only one simple map operation. all others are ignored */
	private String evaluateMapAction(String input) {
		Optional<Mapping> simpleMapping = getMappings(input, Mapping.MappingAction.MAP).stream().findFirst();
		/* we assume only one simple map without context, all others are ignored */
		String name = input;
		if (simpleMapping.isPresent()) {
			name = simpleMapping.get().getOutput();
		}
		return name;
	}
	
	/* get all mappings which has the same action*/
	private List<Mapping> getMappings(String name, Mapping.MappingAction action) {
		List<Mapping> mappings = new ArrayList<>();
		mapping.stream().filter(m -> m.getInput().equalsIgnoreCase(name) && m.getAction().equals(action)).forEach(m -> mappings.add(m));;

		return mappings;
	}
	
	/* return the attribute with name id, ref or rel */
	private Attribute getIdAttribute(Element node) {
		Attribute a = !isNull(node.getAttribute("id")) ? node.getAttribute("id") : node.getAttribute("rel") ;
		return  a != null ? a : node.getAttribute("ref");
	}
	
	/* check if a node has an attribute named id, rel or ref */
	private boolean hasIdAttribute(Element node) {
		return !node.getAttributes().isEmpty() && (!StringUtils.isEmpty(node.getAttributeValue("id")) 
				|| !StringUtils.isEmpty(node.getAttributeValue("rel"))
				|| !StringUtils.isEmpty(node.getAttributeValue("ref")));
	}
	
	private boolean emptyOrCorrectContext(Element n, Mapping m) {
		return StringUtils.isEmpty(m.getContext()) || StringUtils.equalsIgnoreCase(m.getContext(), n.getParentElement().getName());
	}
	
	/* drop nodes from root to n depth */
	private void dropNodes(List<Element> nodes, Element n, int drop) {
		if (drop != 0) {
			for (Element e : n.getChildren()) {
				dropNodes(nodes, e, drop - 1);
			}
		} else {
			nodes.add(n);
		}
	}
	
	/* ignore nodes */
	private boolean isIgnored(String name) {
		return ignoreNodes.contains(name);
	}
	
	/* generating random uuid */
	private UUID getUUID() {
		return UUID.randomUUID();
	}
	
	private boolean isNull(Object o) {
		return o == null;
	}
}
