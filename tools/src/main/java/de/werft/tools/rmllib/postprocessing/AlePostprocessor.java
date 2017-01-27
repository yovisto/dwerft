package de.werft.tools.rmllib.postprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import de.werft.tools.general.Document;

public class AlePostprocessor extends BasicPostprocessor {

	public AlePostprocessor(String projectUri) {
		super(projectUri);
	}

    @Override
   protected Model process(Model model, Document doc) {

    	Resource script = model.getResource(getProjectUri());
    	Property clipsShotProp = model.getProperty("http://filmontology.org/ontology/2.0/clipsShot");
    	
		Property type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Resource clipType = model.getResource("http://filmontology.org/ontology/2.0/Clip");
		
    	ResIterator listResourcesWithProperty = model.listResourcesWithProperty(type, clipType);
    	while (listResourcesWithProperty.hasNext()) {
			Resource resource = (Resource) listResourcesWithProperty.next();
			script.addProperty(clipsShotProp, resource);
		}
    	    	
        return model;
    }

}
