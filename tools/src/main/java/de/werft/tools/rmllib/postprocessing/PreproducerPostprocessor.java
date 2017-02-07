package de.werft.tools.rmllib.postprocessing;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import de.werft.tools.general.Document;

public class PreproducerPostprocessor extends BasicPostprocessor {

	public PreproducerPostprocessor(String projectUri) {
		super(projectUri);
	}
	
	private void fixProductionWithOnlyOneEpisode(Model model) {
    	Resource production = model.getResource(getProjectUri());
    	Property hasEpisode = model.getProperty("http://filmontology.org/ontology/2.0/hasEpisode");

    	int episodeCount = 0;
    	RDFNode lastEpisode = null;
    	NodeIterator it = model.listObjectsOfProperty(production, hasEpisode);
    	while (it.hasNext()) {
    		lastEpisode = it.next();
    		episodeCount++;
    	}
    	
    	String productionType = "";    	
    	
    	if (episodeCount == 1) {
    		productionType = "http://filmontology.org/ontology/2.0/IndividualProduction";
        	ArrayList<Statement> stToRemove = new ArrayList<Statement>();
        	
    		StmtIterator sit = ((Resource)lastEpisode).listProperties();
    		while (sit.hasNext()) {
				Statement st = sit.next();
				
				stToRemove.add(st);
				
				Property predicate = st.getPredicate();
				RDFNode object = st.getObject();
				
				if (!predicate.getURI().equals("http://filmontology.org/ontology/2.0/identifierDramaqueen") &&
						!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
						!predicate.getURI().equals("http://filmontology.org/ontology/2.0/identifierPreProducer")
						) {
				
					Statement newSt = model.createStatement(production, predicate, object);
					model.add(newSt);
				}
				
			}
    		
        	for (Statement statement : stToRemove) {
    			model.remove(statement);
    		}
        	
        	model.remove(production, hasEpisode, lastEpisode);
        	
    	} else {
    		productionType = "http://filmontology.org/ontology/2.0/SeriesProduction";
    	}
		Property type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Resource typeResource = model.getResource(productionType);
		production.removeAll(type);
		production.addProperty(type, typeResource);
		
		
	}

    @Override
    protected Model process(Model model, Document doc) {
    	super.process(model, doc);
    	
    	fixProductionWithOnlyOneEpisode(model);
    	
        return model;
    }
}
