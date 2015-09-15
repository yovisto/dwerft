package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;


public class PreproducerExporter extends RdfExporter {

	/** The Logger. */
	private static final Logger L = Logger.getLogger(PreproducerExporter.class.getName());
	
	private final Namespace PRP_NAMESPACE = Namespace.getNamespace("prp", "http://www.preproducer.com");
	
	private String outputPath;
	private String projectID;
	
	public PreproducerExporter(String sparqlEndpointUrl, String ontologyFileName, String outputPath, String projectID) {
		super(sparqlEndpointUrl, ontologyFileName);
		this.outputPath = outputPath;
		this.projectID = projectID;
	}
	
	public PreproducerExporter(File rdfInput, String outputPath, String projectID) throws IOException {
		super(rdfInput);
		this.outputPath = outputPath;
		this.projectID = projectID;
	}
	
	@Override
	public void export() {
		
		//Prepare xml document template, including root, payload method, and project
		Element root = new Element("root");
		Element methodElement = new Element("payload").setAttribute(new Attribute("method", "postscript"));	
		Element projectIdElement = new Element("project").setAttribute(new Attribute("projectid", projectID));
		
		//Spawn new document and append primary elements
		Document prpXml = new Document(root);
		prpXml.getRootElement().addContent(methodElement.addContent(projectIdElement));
				
		//Get project referenced by the given ID
		List<Resource> listOfProjectsWithSpecifiedID = getResourcesFilteredByLiteral("Project", "identifierPreProducer", projectID);
		Resource project = null;
		//If the the number of projects found is not equal to 1 throw an error
		if (listOfProjectsWithSpecifiedID.size() == 1)
			project = listOfProjectsWithSpecifiedID.get(0);
		else
			L.error("List of projects found matching project ID " + projectID + " is empty or larger than one!");

		//Get all episodes of the current project
		ArrayList<Resource> episodes = getLinkedResources(project, "hasEpisode");
		//And the projects title
		String projectTitle = getLinkedDataValues(project, "title").get(0).getString();
		
		//Iterating over each episode, append the episode element to the project element
		for (Resource episode : episodes) {
			Element episodeElement = getEpisodeElement(episode, projectTitle);
			projectIdElement.addContent(episodeElement);
		}
		
		//Print the final xml file
		writeXmlToFile(prpXml);
	}
	
	/**
	 * Creates an xml element for a given episode resource, which has all its children alrady appended to it
	 * 
	 * @param episode
	 * 				Episode resource which the element will resemble
	 * @param projectTitle
	 * 				Projects title which will in this case mark each episode
	 * @return
	 * 				The Element resembling the episode and it's subelements
	 */
	private Element getEpisodeElement(Resource episode, String projectTitle) {
		
		//Create basic elements
		Element titleElement = new Element("title").setText(projectTitle);
		Element episodeElement = new Element("episode").addContent(titleElement);
		
		//Get all scene groups of the episode
		ArrayList<Resource> sceneGroups = getLinkedResources(episode, "hasSceneGroup");
		
		//Iterating over each scene group, append the scene group element to the episode element
		for (Resource sceneGroup : sceneGroups) {
			Element sceneGroupElement = getSceneGroupElement(sceneGroup);
			episodeElement.addContent(sceneGroupElement);
		}
		return episodeElement;
	}
	
	private Element getSceneGroupElement(Resource sceneGroup) {
		
		//Create the basic element
		Element sceneGroupElement = new Element("scene-group");
		
		//Get all scenes of the scene group
		ArrayList<Resource> scenes = getLinkedResources(sceneGroup, "hasScene");
		
		//Iterating over each scene, append the scene element to the scene group element
		for (Resource scene : scenes) {
			Element sceneElement = getSceneElement(scene);
			sceneGroupElement.addContent(sceneElement);
		}
		return sceneGroupElement;
	}

	private Element getSceneElement(Resource scene) {
		
		//Create the basic element
		Element sceneElement = new Element("scene");
		Map<String, ArrayList<Literal>> allEpisodeDataValues = getAllLinkedDataValues(scene);
		
		//All elements are hard coded, might be better to find a genralized way of appending these
		//It is assumed the Arraylist only contains one value for each key
		
		ArrayList<Literal> literalList = null;
		
		//interior/exterior
		literalList = allEpisodeDataValues.get("interiorExterior");
		if (literalList != null) {
			String intExt = literalList.get(0).getString();
			Element intExtElement = new Element("intext").setText(intExt);
			sceneElement.addContent(intExtElement);
		}
		
		//day/night
		literalList = allEpisodeDataValues.get("dayTime");
		if (literalList != null) {
			String dayNight = literalList.get(0).getString();
			Element dayNightElement = new Element("daynight").setText(dayNight);
			sceneElement.addContent(dayNightElement);
		}
		
		
		//TODO add missing elements
		//
		
		
		return sceneElement;
	}

	/**
	 * recursively adds namespace prefixes to all elements
	 * 
	 * @param root the root element
	 */
	private void addNameSpaces(Element root) {
		
	    if(root != null) {
		    root.setNamespace(PRP_NAMESPACE);
		    for (int index = 0; root.getChildren() != null &&  index < root.getChildren().size(); index++) {
		        addNameSpaces((Element) root.getChildren().get(index));
		    }
	    }
	}
	
	/**
	 * Writes the xml document in pretty print to console and a file
	 */
	private void writeXmlToFile(Document prpXml) {
		try {
			XMLOutputter xmlOutput = new XMLOutputter();  
			xmlOutput.setFormat(Format.getPrettyFormat());  
			xmlOutput.output(prpXml, System.out);
			xmlOutput.output(prpXml, new FileWriter(outputPath)); 
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	
}
