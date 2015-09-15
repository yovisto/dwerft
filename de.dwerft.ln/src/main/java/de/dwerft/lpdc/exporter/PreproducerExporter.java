package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

import de.dwerft.lpdc.importer.general.Mapping;
import de.dwerft.lpdc.importer.general.Mapping.MappingAction;


public class PreproducerExporter extends RdfExporter {

	private final Namespace PRP_NAMESPACE = Namespace.getNamespace("prp", "http://www.preproducer.com");
	
	private String outputPath;
	private String projectID;
	private HashSet<Mapping> mapping;
	
	private Document prpXml;
	
	public PreproducerExporter(File rdfInput, HashSet<Mapping> mapping, String outputPath, String projectID) throws IOException {
		super(rdfInput);
		this.mapping = mapping;
		this.outputPath = outputPath;
		this.projectID = projectID;
	}
	
	@Override
	public void export() {
			writeSceneXml();
	}

	private void writeSceneXml() {
			
		Element methodElement = prepareXml("postScript");
			
		//TODO Get correct IDs
		Element projectIdElement = new Element("project").setAttribute(new Attribute("projectid", projectID));
		Element episodeElement = new Element("episode").setAttribute(new Attribute("id", ""));
		//Element titleElement = new Element("title").setText(text);
		Element sceneGroupElement = new Element("scene-group");
		
		//Add primary elements to document
		methodElement.addContent(projectIdElement.addContent(episodeElement.addContent(sceneGroupElement)));

		
		//Generate scene xml		
		ResultSet sceneResults = ResultSetFactory.fromXML(getScenesAsXML(projectID));
		
		//Generate scene elements
		while (sceneResults.hasNext()) {
			
			QuerySolution scene = sceneResults.next();
			Iterator<String> varNames = scene.varNames();
			
			Element sceneElement = new Element("scene");
			
			while (varNames.hasNext()) {
				
				String var = varNames.next();
				String varValue = getResourceOrLiteralValue(scene, var);
				String elementName = evaluateMapAction(var.substring(0, var.length()-1)); //remove the plural s appended during the SPARQL query
				sceneElement.addContent(new Element(elementName).setText(varValue));
				
			}
			
			//Add scene element
			sceneGroupElement.addContent(sceneElement);
		}
		
		addNameSpaces(projectIdElement);
		writeXmlToFile();
	}
	
	/* we assume that there is only one simple map operation. all others are ignored */
	private String evaluateMapAction(String input) {
		Optional<Mapping> simpleMapping = getMappings(input, MappingAction.MAP).stream().findFirst();
		/* we assume only one simple map without context, all others are ignored */
		String name = input;
		if (simpleMapping.isPresent()) {
			name = simpleMapping.get().getInput();
		}
		return name;
	}
	
	/* get all mappings which has the same action*/
	private List<Mapping> getMappings(String name, MappingAction action) {
		List<Mapping> mappings = new ArrayList<>();
		mapping.stream().filter(m -> m.getOutput().equalsIgnoreCase(name) && m.getAction().equals(action)).forEach(m -> mappings.add(m));;

		return mappings;
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
	 * Adds the root element and method info to the global xml document
	 * 
	 * @param methodName
	 * @return the current element for further processing
	 */
	private Element prepareXml(String methodName) {
		
		Element root = new Element("root");
		root.addNamespaceDeclaration(PRP_NAMESPACE);
		Element methodElement = new Element("payload").setAttribute(new Attribute("method", methodName));
		
		prpXml = new Document(root);
		//prpXml.setRootElement(root);
		prpXml.getRootElement().addContent(methodElement);
		
		return methodElement;
	}
	
	/**
	 * Writes the xml document in pretty print to console and a file
	 */
	private void writeXmlToFile() {
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
