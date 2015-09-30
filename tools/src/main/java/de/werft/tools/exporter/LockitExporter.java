package de.werft.tools.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class LockitExporter.
 * A reference implementation generating a valid CSV file which can be read by the Lockit API.
 * Currently the CSV file only contains data concerning scenes.
 * The data is fetched by querying the SPARQL endpoint and utilizing the methods provided in the parent abstract class.
 */
public class LockitExporter extends RdfExporter {
	
	/** The Logger. */
	private static final Logger L = LogManager.getLogger(LockitExporter.class);
	
	/** The output path. */
	private String outputPath;
	
	/** The project id. */
	private String projectId;
	
	/**
	 * Instantiates a new lockit exporter.
	 *
	 * @param sparqlEndpointUrl 
	 * 			SPARQL endpoint url, typically http://sparql.filmontology.org
	 * @param ontologyFileName 
	 * 			file containing the ontology model
	 * @param outputPath 
	 * 			path to resulting XML file
	 * @param projectID 
	 * 			the project ID used internally by the LPDC
	 */
	public LockitExporter(String sparqlEndpointUrl, String ontologyFilename, String outputPath, String projectId) {
		super(sparqlEndpointUrl, ontologyFilename);
		this.outputPath = outputPath;
		this.projectId = projectId;
	}
	
	/* (non-Javadoc)
	 * @see de.werft.tools.exporter.RdfExporter#export()
	 */
	@Override
	public void export() {
		try {
			L.info("Writing Lockit CSV file to " + outputPath);
			FileWriter writer = new FileWriter(outputPath);
			
			List<Resource> scenes = getAllScenesFromProject();
			
			//Iterate over each scene and add a line to the resulting csv file with all values being separated by a semicolon
			for (Resource scene : scenes) {
				
				Map<String, ArrayList<Literal>> literals = getAllLinkedDataValues(scene);
				
				writer.append(getValueFromLiteral(literals, "sceneNumber") + ";");
				
				String intExt = getValueFromLiteral(literals, "interiorExterior");
				String dayNight = getValueFromLiteral(literals, "dayTime");
				writer.append(createLockitIAT(intExt, dayNight) + ";");
				
				writer.append(getValueFromLiteral(literals, "estimatedTime") + ";");
				writer.append(getValueFromLiteral(literals, "sceneHeader") + ";");
				writer.append(getValueFromLiteral(literals, "sceneDescription") + ";");
				writer.append("\n");
			}
			
			writer.flush();
			writer.close();
		} catch (IOException io) {
			L.error("Could not write csv. " + io.getMessage());
		}
	}
	
	/**
	 * Gets the literal value from the first found literal or an empty string
	 *
	 * @param literals 
	 * 			The literals
	 * @param propertyName 
	 * 			The name of the property in question
	 * @return 
	 * 			The literal's value or an empty String
	 */
	private String getValueFromLiteral(Map<String, ArrayList<Literal>> literals, String propertyName) {
		if (literals.containsKey(propertyName)) {
			return literals.get(propertyName).get(0).getString();
		} else {
			return "";	
		}
	}

	/**
	 * Gets the all scenes from project.
	 *
	 * @return the all scenes from project
	 */
	private List<Resource> getAllScenesFromProject() {
		List<Resource> scenes = new ArrayList<Resource>();
		List<Resource> project = getResourcesFilteredByLiteral("Project", "identifierPreProducer", projectId);
		
		// if we have a project we seek for the episodes and collect all scenes
		if (!project.isEmpty()) {
			List<Resource> episodes = getLinkedResources(project.get(0), "hasEpisode");
		
			for (Resource r : episodes) {
				List<Resource> sceneGroup = getLinkedResources(r, "hasSceneGroup");
				
				for (Resource s : sceneGroup) {
					scenes.addAll(getLinkedResources(s, "hasScene"));
				}
			}
		}
		return scenes;
	}
	
	/**
	 * Generates the formatted String required by Lockit
	 * The String contains intExt and dayNight information.
	 *
	 * @param intExt 
	 * 		Interior/Exterior
	 * @param dayNight
	 * 		Day/Night
	 * @return 
	 * 		String containing the given data in a format readable by Lockit
	 */
	private String createLockitIAT(String intExt, String dayNight) {
		
		StringBuilder sb = new StringBuilder();
		
		if (intExt.matches("int(ext)?"))
			sb.append("I/");
		if (intExt.matches("(int)?ext"))
			sb.append("A/");
		
		if (dayNight.equalsIgnoreCase("day"))
			sb.append("T");
		else
			sb.append("N");
		
		return sb.toString();
	}
}
