package de.dwerft.lpdc.importer.general;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The XMLProcessor processes an XML document with the DOM model and traverses
 * the document in an recursive fashion element by element.
 * 
 * @author hagt
 *
 */
public class XMLProcessor {
	
	private Element documentElement;
	private Node currentNode = null;
	
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
	
	/**
	 * Builds the path of an XML node up to the root by getting parent elements names.
	 * 
	 * @param node	Node to be evaluated.
	 * @return Path from the document root to the input node.
	 */
	public static String getXmlPath(Node node) {
		
		String result = node.getNodeName();
		
		Node current = node;
		
		while((current = current.getParentNode()) != null) {
			if (!current.getNodeName().equals("#document")) {
				result = current.getNodeName()+"/"+result;
			}
		}
		
		return "/"+result;
		
	}

	/**
	 * Creates a new XMLProcessor and parses the XML document.
	 * 
	 * @param inputStream	Input stream of the XML document.
	 */
	public XMLProcessor(InputStream inputStream) {
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(inputStream);
			
			this.documentElement = doc.getDocumentElement();
			
		} catch ( ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves the next element from the XML document. If an element has
	 * childs the whole tree will be traversed down the leaf.
	 * 
	 * @return	The current XML node.
	 */
	public Node nextElement() {
		if (currentNode == null) {
			currentNode = documentElement;
		} else {
			
			Node firstChild = currentNode.getFirstChild();
			
			if (firstChild != null) {
				currentNode = firstChild;
			} else {
				Node nextSibling = currentNode.getNextSibling();
				if (nextSibling != null) {
					currentNode = nextSibling;
				} else {
					boolean found = false;
					
					while(!found) {
						Node parentNode = currentNode.getParentNode();
						if (parentNode == null) {
							return null;
						}
						Node sib = parentNode.getNextSibling();
						if (sib == null) {
							currentNode = parentNode;
						} else {
							currentNode = sib;
							found = true;
						}
					}
				}
			}
		}
		
		return currentNode;
	}

	/**
	 * Get the root document element.
	 * 
	 * @return The root document element.
	 */
	public Element getDocumentElement() {
		return documentElement;
	}
	
}
