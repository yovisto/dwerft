package de.werft.tools.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class LockitExporter extends RdfExporter {
	
	private String outputPath;
	
	private String projectId;
	
	public LockitExporter(String sparqlEndpointUrl, String ontologyFilename, String outputPath, String projectId) throws IOException {
		super(sparqlEndpointUrl, ontologyFilename);
		this.outputPath = outputPath;
		this.projectId = projectId;
	}
	
	@Override
	public void export() {
		try {
			FileWriter writer = new FileWriter(outputPath);
			
			List<Resource> scenes = getAllScenesFromProject();
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
			System.out.println("Could not write csv. " + io.getMessage());
		}
	}
	
	// returns the literal value from the first found literal or an empty string
	private String getValueFromLiteral(Map<String, ArrayList<Literal>> literals, String propertyName) {
		if (literals.containsKey(propertyName)) {
			return literals.get(propertyName).get(0).getString();
		} else {
			return "";	
		}
	}
	
	// retuns all scenes for a project
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
	 * The String contains intExt and dayNight information
	 * 
	 * @param intExt
	 * @param dayNight
	 * @return
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
