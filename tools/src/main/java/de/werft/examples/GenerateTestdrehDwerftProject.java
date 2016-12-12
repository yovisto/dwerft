package de.werft.examples;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class GenerateTestdrehDwerftProject {

	public static void main(String[] args) {
		
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("for", "http://filmontology.org/resource/");
        m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");
        
        OntModel ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        InputStream is = null;
        
        try {
            is = new BufferedInputStream(new FileInputStream("ontology/dwerft-ontology_v2.owl"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ontologyModel.read(is, "RDF/XML");
        
        Model model = ModelFactory.createDefaultModel();
        
        model.setNsPrefixes(ontologyModel.getNsPrefixMap());
        
        String tc = "01:00:00:00";
        
        Resource dwerft = model.createResource("http://filmontology.org/resource/DWERFT");
        Property part = model.createProperty("http://purl.org/dc/terms/hasPart");
        Resource project = model.createResource("http://filmontology.org/resource/DwerftProject/c08a2dc4-728b-40d6-b1e0-ef510528a53f"+"#smtpe"+tc);
        
        dwerft.addProperty(part, project);
        
		OutputStream out;
		try {
			out = new FileOutputStream("project_test.ttl");
            RDFDataMgr.write(out, model, Lang.TTL);
			out.close();
		} catch (IOException e) {
		}        
        
        
	}

}
