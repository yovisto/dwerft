package de.werft.tools.rmllib.postprocessing;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.werft.tools.general.Document;

/**
 * Post process dramaqueen documents.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class DramaqueenPostprocessor extends BasicPostprocessor {
	
	private String getScriptId(org.w3c.dom.Document document) {
		Element scriptDoc = (Element)document.getElementsByTagName("ScriptDocument").item(0);
		return scriptDoc.getAttributes().getNamedItem("id").getTextContent();
	}

	private String getArchiveId(org.w3c.dom.Document document) {
		String result = null;

		boolean found = false;
        
        NodeList nodes = document.getElementsByTagName("Plot");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String archiveId = node.getAttributes().getNamedItem("id").getTextContent();
            
            NodeList propChilds = ((Element)node).getElementsByTagName("Property");
            for (int j = 0; j < propChilds.getLength(); j++) {
            	NamedNodeMap attributes = propChilds.item(j).getAttributes();
            	String idValue = attributes.getNamedItem("id").getTextContent();
            	if ("archive".equals(idValue)) {
            		found = true;
            		break;
            	}
            }
            if (found) {
            	result = archiveId;
            	break;
            }
        }
        return result;
	}

    private void buildSceneNumbering(Model model, org.w3c.dom.Document document) {
    	
        Element scriptDoc = (Element)document.getElementsByTagName("ScriptDocument").item(0);
        Element storyChildren = getFirstChildByTagName(scriptDoc, "children");
        NodeList proxies = storyChildren.getElementsByTagName("Proxy");
        int sceneNumber = 1;
        int sceneSplit = 1;
        for (int i = 0; i < proxies.getLength(); i++) {
        	Node proxy = proxies.item(i);
        	String stepId = proxy.getAttributes().getNamedItem("proxy_for").getTextContent();
        	Element step = getElementByTagNameAndId(document,"Step",stepId);
        	NodeList scenes = step.getElementsByTagName("Frame");
        	
    		boolean split = false;
        	
        	for (int j = 0; j < scenes.getLength(); j++) {
        		Node scene = scenes.item(j);
        		String sceneId = scene.getAttributes().getNamedItem("id").getTextContent();
        		Node parentGroup = scene.getParentNode().getParentNode();
        		if (((Element)parentGroup).getElementsByTagName("Frame").getLength() > 1)  {
        			split = true;
        		} else {
        			if (split) {
        				sceneNumber++;
        			}
        			split = false;
        			sceneSplit = 1;
        		}
        		Resource rdfScene = model.getResource("http://filmontology.org/resource/Scene/"+sceneId);
	           	Property property = model.getProperty("http://filmontology.org/ontology/2.0/sceneNumber");

        		if (split) {
        			rdfScene.addProperty(property, Integer.toString(sceneNumber)+"."+Integer.toString(sceneSplit));
        			System.out.println(sceneNumber+"."+sceneSplit+" - "+sceneId + "  "+split);
        			sceneSplit++;
        		} else {
        			rdfScene.addProperty(property, Integer.toString(sceneNumber));
            		System.out.println(sceneNumber+" - "+sceneId + "  "+split);
            		sceneNumber++;
        		}
			}
		}
    }
    
    /*
	private boolean isSceneVisible(Node node) {
		boolean isVisibleInStory = false;
		boolean isInArchive = false;
		int manualExclusion = 1 << 1;
		
		NodeList props = ((Element)node).getElementsByTagName("Property");
		for (int i = 0; i < props.getLength(); i++) {
			Node prop = props.item(i);
			if (prop.getAttributes().getNamedItem("id").getTextContent().equals("plot")) {
				NodeList options = ((Element)prop).getElementsByTagName("Option");
				for (int j = 0; j < options.getLength(); j++) {
					
				}
				
			}
		}
		
		
		NodeList props = node.getChildNodes();
		for (int i = 0; i < props.getLength(); i++) {
			Node prop = props.item(i);
			if ("Property".equals(prop.getNodeName()) && "11".equals(getValueOfAttribute(prop, "id"))) {
				
				NodeList childNodes = prop.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node ref = childNodes.item(j);
					if (projectIdentifier.equals(getValueOfAttribute(ref, "value"))) {
						int flags = Integer.valueOf(getValueOfAttribute(ref, "flags"));
						isVisibleInStory = (flags & manualExclusion) == 0;
					} else if (archiveId.equals(getValueOfAttribute(ref, "value"))) {
						int flags = Integer.valueOf(getValueOfAttribute(ref, "flags"));
						isInArchive = (flags & manualExclusion) == 0;
					}
				}
			}
		}
		
		return isVisibleInStory && !isInArchive;
}
    */
    
    
    private Element getFirstChildByTagName(Node node, String tagName) {
    	Element result = null;
    	
    	NodeList childNodes = node.getChildNodes();
    	
    	for (int i = 0; i < childNodes.getLength(); i++) {
    		Node item = childNodes.item(i);
    		if (tagName.equals(item.getNodeName())) {
    			result = (Element)item;
    			break;
    		}
		}
    	return result;
    	
    }
    
    
    private Element getElementByTagNameAndId(org.w3c.dom.Document document, String tagName, String searchId) {
    	Element result = null;
    	NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String id = node.getAttributes().getNamedItem("id").getTextContent();
            if (searchId.equals(id)) {
            	result = (Element) node;
            	break;
            }
        }    	
    	return result;
    }
    
    @Override
    protected Model process(Model model, Document doc) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            /* get the root node */
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(doc.getInputFile().getFile());
            
            buildSceneNumbering(model, document);
            
        } catch (SAXException | IOException e) {
            logger.error("Could not post process dramaqueen xml. " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error("Could not initialize xml parser. " + e.getMessage());
        }

        return model;
    }
}
