package de.werft.tools.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.werft.tools.general.OntologyConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The Class PreproducerExporter.
 * A reference implementation generating a valid XML file which can be read by the preproducer API.
 * Currently the XML file only contains data concerning scenes.
 * The data is fetched by querying the SPARQL endpoint and utilizing the methods provided in the parent abstract class.
 */
public class PreproducerExporter extends RdfExporter {

	/** The Logger. */
	private static final Logger L = LogManager.getLogger(PreproducerExporter.class);
	
	/** The prp namespace. */
	private final Namespace PRP_NAMESPACE = Namespace.getNamespace("prp", "http://www.preproducer.com/");
	
	/** The output path. */
	private String outputPath;
	
	/** The project id. */
	private String projectID;
	
	/** The target project id. */
	private String targetProjectID;
		
	/**
	 * Instantiates a new preproducer exporter.
	 *  @param sparqlEndpointUrl
	 * 			SPARQL endpoint url, typically http://sparql.filmontology.org
	 * @param ontologyFileName
	 * 			file containing the ontology model
     * @param outputPath
 * 			path to resulting XML file
     * @param projectID
* 			the project ID used internally by the LPDC
     * @param targetProjectID
     */
	public PreproducerExporter(String sparqlEndpointUrl, InputStream ontologyFileName, String outputPath, String projectID, String targetProjectID) {
		super(sparqlEndpointUrl, ontologyFileName);
		this.outputPath = outputPath;
		this.projectID = projectID;
		this.targetProjectID = targetProjectID;
	}
	
	/* (non-Javadoc)
	 * @see de.werft.tools.exporter.RdfExporter#export()
	 */
	@Override
	public void export() {
		//In this example currently only a scene XMl is generated
		exportScenes();
	}
	
	/**
	 * Exports all scenes for the projectID given in the constructor.
	 */
	private void exportScenes() {
		
		//Prepare xml document template, including root, payload method, and project
		Element root = new Element("root");
		root.addNamespaceDeclaration(PRP_NAMESPACE);
		Element methodElement = new Element("payload").setAttribute(new Attribute("method", "postScript"));	
		Element projectIdElement = new Element("project").setAttribute(new Attribute("projectid", targetProjectID));
		
		//Spawn new document and append primary elements
		Document prpXml = new Document(root);
		prpXml.getRootElement().addContent(methodElement.addContent(projectIdElement));
				
		//Get project referenced by the given ID
		List<Resource> listOfProjectsWithSpecifiedID = getResourcesFilteredByLiteral("Project", "identifierPreProducer", projectID);
		listOfProjectsWithSpecifiedID.addAll(getResourcesFilteredByLiteral("Project", "identifierDramaQueen", projectID));
		Resource project = null;
		
		//If the number of projects found is not equal to 1 throw an error
		if (listOfProjectsWithSpecifiedID.size() == 1) {
			L.info("Exporting scenes for projectID " + projectID);
			project = listOfProjectsWithSpecifiedID.get(0);
		} else {
			L.error("List of projects found matching project ID " + projectID + " is empty or larger than one!");
		}
		
		//Get all episodes of the current project
		ArrayList<Resource> episodes = getLinkedResources(project, "hasEpisode");
		
		//And the projects title
		String projectTitle = "";
		if (!getLinkedDataValues(project, "title").isEmpty())
			projectTitle = getLinkedDataValues(project, "title").get(0).getString();
		
		//Iterating over each episode, append the episode element to the project element
		for (Resource episode : episodes) {	
			Element episodeElement = getEpisodeElement(episode, projectTitle);
			projectIdElement.addContent(episodeElement);
		}
		
		//Add namespaces to all nodes starting with the root
		addNameSpaces(root);
		
		//Print the final xml file
		writeXmlToFile(prpXml);
	}
	
	/**
	 * Creates an xml element for a given episode resource, which has all its children already appended to it.
	 *
	 * @param episode 				
	 * 			Episode resource which the element will resemble
	 * @param projectTitle 				
	 * 			Projects title which will in this case mark each episode
	 * @return 				
	 * 			The Element resembling the episode and it's subelements
	 */
	private Element getEpisodeElement(Resource episode, String projectTitle) {
		
		//Create basic elements
		Element titleElement = new Element("title").setText(projectTitle);
		Element episodeElement = new Element("episode").addContent(titleElement);
		
		//Add episode ID as attribute if there is one
		List<Literal> episodeIDs = getLinkedDataValues(episode, "identifierPreProducer");
		if (!episodeIDs.isEmpty())
			episodeElement.setAttribute("id", episodeIDs.get(0).getString());
		
		//Get all scene groups of the episode
//		ArrayList<Resource> sceneGroups = getLinkedResources(episode, "hasSceneGroup");

		//////////////////////////////////////////////////////////////////////////////
		//TODO Scene-Groups must be in correct order for preproducer import. This is a hack, because ordering for scene group is difficult right now
		ArrayList<Resource> sceneGroups = new ArrayList<Resource>();
		String orderByQuery = OntologyConstants.ONTOLOGY_PREFIXES
				+ "select ?group where { "
				+ "<"+episode.getURI()+"> "
				+ OntologyConstants.ONTOLOGY_PREFIX+":hasSceneGroup ?group. "
				+ "?group "+ OntologyConstants.ONTOLOGY_PREFIX+":internalIdentifier ?internalIdentifier ."
				+ "}ORDER BY ASC(?internalIdentifier)";
		ResultSet rs = queryEndpoint(orderByQuery);
		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			sceneGroups.add(sol.getResource("group"));
		}
		//////////////////////////////////////////////////////////////////////////////
		
		//Iterating over each scene group, append the scene group element to the episode element
		for (Resource sceneGroup : sceneGroups) {
			Element sceneGroupElement = getSceneGroupElement(sceneGroup);
			episodeElement.addContent(sceneGroupElement);
		}
		return episodeElement;
	}
	
	/**
	 * Gets the scene group element.
	 *
	 * @param sceneGroup 
	 * 			the scene group
	 * @return 
	 * 			the scene group element
	 */
	private Element getSceneGroupElement(Resource sceneGroup) {
		
		//Create the basic element
		Element sceneGroupElement = new Element("scene-group");
		
		//Get all scenes of the scene group
		ArrayList<Resource> scenes = getLinkedResources(sceneGroup, "hasScene");
		
		//Sort scenes by number
		scenes.sort(new Comparator<Resource>() {

			//Custom comparator, since scene number may be in the form of 1a, 2b etc..
			@Override
			public int compare(Resource scene1, Resource scene2) {
				String sceneNumber1 = getLinkedDataValues(scene1, "sceneNumber").get(0).getLexicalForm();
				String sceneNumber2 = getLinkedDataValues(scene2, "sceneNumber").get(0).getLexicalForm();
				
				if (isNumberOnly(sceneNumber1) && isNumberOnly(sceneNumber2)) {
					return (Integer.compare(Integer.parseInt(sceneNumber1), Integer.parseInt(sceneNumber2)));
				} else {
					int s1 = getCompoundNumberDigits(sceneNumber1);
					int s2 = getCompoundNumberDigits(sceneNumber2);
					if (Integer.compare(s1, s2) != 0) {
						return (Integer.compare(s1, s2));
					} else {
						return getCompundNumberLetters(sceneNumber1).compareToIgnoreCase(getCompundNumberLetters(sceneNumber2));
					}
				}
			}
			
			/**
			 * Checks if a given String is an Integer or not
			 * @param sceneNumber
			 * 			the String to be checked
			 * @return
			 */
			private Boolean isNumberOnly(String sceneNumber) {
				try {
					Integer.parseInt(sceneNumber);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
			
			//Gets the number part of a compound scene number
			private int getCompoundNumberDigits(String sceneNumber) {
				return Integer.parseInt(sceneNumber.replaceAll("[a-zA-Z]+", ""));
			}
			
			//Gets the letter part of a compound scene number
			private String getCompundNumberLetters(String sceneNumber) {
				return sceneNumber.replaceAll("\\d+", "");
			}	
		});
		
		//Iterating over each scene, append the scene element to the scene group element
		for (Resource scene : scenes) {
			Element sceneElement = getSceneElement(scene);
			sceneGroupElement.addContent(sceneElement);
		}
		return sceneGroupElement;
	}

	/**
	 * Gets the scene element.
	 * TODO Adding content could be changed to an iterator using the mappings instead of hard coded values
	 *
	 * @param scene the scene
	 * @return the scene element
	 */
	private Element getSceneElement(Resource scene) {
		
		//Create the basic element
		Element sceneElement = new Element("scene");
		Map<String, ArrayList<Literal>> allSceneDataValues = getAllLinkedDataValues(scene);
		
		//All elements are hard coded, might be better to find a generalized way of appending these
		//It is assumed the Arraylist only contains one value for each key
		
		//scene decoration name | this is an attribute
		ArrayList<Resource> sets = getLinkedResources(scene, "sceneSet");
		if (sets.size() == 1) {
			String name = getLinkedDataValues(sets.get(0), "name").get(0).getString();
			sceneElement.setAttribute("decorationname", name);
		}
			
		//scene inserted | this is an attribute
		if (allSceneDataValues.containsKey("sceneInserted")) {
			String insertedText = getSceneContentValue(allSceneDataValues, "sceneInserted");
			String insertedBoolean = "true";
			if (insertedText.equals("0"))
				insertedBoolean = "false";
			
			sceneElement.setAttribute("insert", insertedBoolean);
		}
					
		//scene number
		if (allSceneDataValues.containsKey("sceneNumber"))
			sceneElement.addContent(new Element("number").setText(getSceneContentValue(allSceneDataValues, "sceneNumber")));
		
		//interior/exterior
		if (allSceneDataValues.containsKey("interiorExterior"))
			sceneElement.addContent(new Element("intext").setText(getSceneContentValue(allSceneDataValues, "interiorExterior")));
		
		//day/night
		if (allSceneDataValues.containsKey("dayTime"))
			sceneElement.addContent(new Element("daynight").setText(getSceneContentValue(allSceneDataValues, "dayTime")));
		
		//description
		if (allSceneDataValues.containsKey("sceneDescription"))
			sceneElement.addContent(new Element("description").setText(getSceneContentValue(allSceneDataValues, "sceneDescription")));
		
		//script | is nested in preproducer but not in rdf
		if (allSceneDataValues.containsKey("sceneContent")) {
			Element scriptElement = new Element("script").addContent(new Element("formattedscript").setText(getSceneContentValue(allSceneDataValues, "sceneContent")));
			sceneElement.addContent(scriptElement);
		}
		//scene shots
		if (allSceneDataValues.containsKey("sceneShots"))
			sceneElement.addContent(new Element("shots").setText(getSceneContentValue(allSceneDataValues, "sceneShots")));
			
		//story time
		if (allSceneDataValues.containsKey("storyTime"))
			sceneElement.addContent(new Element("storytime").setText(getSceneContentValue(allSceneDataValues, "sceneShots")));
		
		//estimated time
		if (allSceneDataValues.containsKey("estimatedTime"))
			sceneElement.addContent(new Element("prestop").setText(getSceneContentValue(allSceneDataValues, "estimatedTime")));
		
		//pages
		if (allSceneDataValues.containsKey("pages"))
			sceneElement.addContent(new Element("pages").setText(getSceneContentValue(allSceneDataValues, "pages")));
		
		//page start
		if (allSceneDataValues.containsKey("pageStart"))
			sceneElement.addContent(new Element("pagefrom").setText(getSceneContentValue(allSceneDataValues, "pageStart")));
		
		//page end
		if (allSceneDataValues.containsKey("pageEnd"))
			sceneElement.addContent(new Element("pageto").setText(getSceneContentValue(allSceneDataValues, "pageEnd")));
		
		return sceneElement;
	}
	
	/**
	 * Gets the actual value of a literal
	 *
	 * @param allSceneDataValues 
	 * 			all literals of a scene
	 * @param ontologyIdentifier 
	 * 			the identifier of a certain literal 
	 * @return 
	 * 			the value of the given literal
	 */
	private String getSceneContentValue(Map<String, ArrayList<Literal>> allSceneDataValues, String ontologyIdentifier) {
		return allSceneDataValues.get(ontologyIdentifier).get(0).getLexicalForm();
	}

	/**
	 * recursively adds namespace prefixes to all elements.
	 *
	 * @param root the root element
	 */
	private void addNameSpaces(Element root) {
		
	    if(root != null) {
		    if (hasNamespace(root))
	    		root.setNamespace(PRP_NAMESPACE);
		    
		    for (int index = 0; root.getChildren() != null &&  index < root.getChildren().size(); index++) {
		        addNameSpaces((Element) root.getChildren().get(index));
		    }
	    }
	}

	/**
	 * Returns true if the given element is supposed to have a namespace prefix and false otherwise.
	 *
	 * @param root 			
	 * 			the element in question
	 * @return 
	 * 			true, if successful
	 */
	private boolean hasNamespace(Element root) {
		return !(root.getName().equals("title") || root.getName().equals("root") || root.getName().equals("payload"));
	}
	
	
	/**
	 * Writes the xml document in pretty print to console and a file.
	 *
	 * @param prpXml 
	 * 		the XML document to be printed
	 */
	private void writeXmlToFile(Document prpXml) {
		try {
			Format prettyFormat = Format.getPrettyFormat();
			XMLOutputter xmlOutput = new XMLOutputter(prettyFormat);
			xmlOutput.output(prpXml, System.out);
			xmlOutput.output(prpXml, new FileWriter(outputPath));
			
			String content = new String(Files.readAllBytes(Paths.get(outputPath)));
			
			content = content.replaceAll("&lt;", "<");
			content = content.replaceAll("&gt;", ">");
			
			Files.write(Paths.get(outputPath), content.getBytes());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
}
