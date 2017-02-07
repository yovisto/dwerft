package de.werft.tools.rmllib.postprocessing;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import be.ugent.mmlab.rml.model.dataset.StdRMLDataset;
import de.werft.tools.general.Document;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the basic access functions to manipulate the source
 * file and the result model. No real postprocessing is done, so subclass to provide
 * some.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class BasicPostprocessor implements Postprocessor {

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    protected final String projectUri;

    public String getProjectUri() {
		return projectUri;
	}

	public BasicPostprocessor(String projectUri) {
        this.projectUri = projectUri;
    }

    @Override
    public RMLDataset postprocess(RMLDataset dataset, Document doc) {
        try {
            Model m = convertToModel(dataset);
            process(m, doc);
            return convertFromModel(m);
        } catch (IOException e) {
            logger.error("Postprocessing failed. Message: " + e.getMessage());
        }

        return null;
    }

    /* store a rml dataset as tmp file and load as jena model */
    private Model convertToModel(RMLDataset dataset) throws IOException {
        Path tmp = Files.createTempFile("toModel", ".ttl");
        dataset.dumpRDF(Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING), RDFFormat.TURTLE);
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, Files.newInputStream(tmp), Lang.TURTLE);
        return m;
    }

    private RMLDataset convertFromModel(Model m) throws IOException {
        Path tmp = Files.createTempFile("fromModel", ".ttl");
        RDFDataMgr.write(Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING), m, Lang.TTL);
        RMLDataset dataset = new StdRMLDataset(false);
        dataset.addFile(tmp.toString(), RDFFormat.TURTLE);
        return dataset;
    }
    
	private void fixBlankNodes(Model model) {

    	Set<Statement> toRemove = new HashSet<Statement>(); 
		
    	Property hasTitle = model.getProperty("http://filmontology.org/ontology/2.0/hasTitle");

    	// Fix duplicate title blank nodes
		ResIterator hasTitleIter = model.listResourcesWithProperty(hasTitle);
		while (hasTitleIter.hasNext()) {
			Resource resource = (Resource) hasTitleIter.next();
			
			StmtIterator listProperties = resource.listProperties(hasTitle);
			listProperties.next();
			while (listProperties.hasNext()) {
				Statement statement = (Statement) listProperties.next();
				toRemove.add(statement);
			}
			
		}
		
		for (Statement statement : toRemove) {
			model.remove(statement);
		}
		toRemove.clear();		
	

    	// Fix not linked blank nodes
		
		Set<Resource> blankNodes = new HashSet<Resource>();
		Set<Resource> linkedBlankNodes = new HashSet<Resource>();
		
		ResIterator listSubjects = model.listSubjects();
		while (listSubjects.hasNext()) {
			Resource resource = (Resource) listSubjects.next();
			if (resource.isAnon()) {
				blankNodes.add(resource);
			}
		}
		
		StmtIterator listStatements2 = model.listStatements();
		while (listStatements2.hasNext()) {
			Statement statement = (Statement) listStatements2.next();
			if (blankNodes.contains(statement.getObject())) {
				linkedBlankNodes.add((Resource) statement.getObject());
			}
		}
		
		blankNodes.removeAll(linkedBlankNodes);
		
		StmtIterator listStatements = model.listStatements();			
		while (listStatements.hasNext()) {
			Statement statement = (Statement) listStatements.next();
			
			if (blankNodes.contains(statement.getSubject())) {
				toRemove.add(statement);
			}
		}

		for (Statement statement : toRemove) {
			model.remove(statement);
		}

	}

    /**
     * Override this method to post process the rdf model.
     *
     * @param model - the rdf jena model
     * @param doc - the document
     * @return a changed model
     */
    protected Model process(Model model, Document doc) {
    	fixBlankNodes(model);
        return model;
    }
}
