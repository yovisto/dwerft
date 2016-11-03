package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.general.Document;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * This class serves as a basic extension point for all preprocessor
 * implementations. It provides some basic functionality like adding
 * the RML source files to the mapping at runtime.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class BasicPreprocessor implements Preprocessor {

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    private static final String RML_RESOURCE = "http://semweb.mmlab.be/ns/rml#logicalSource";

    private static final String RML_SOURCE = "http://semweb.mmlab.be/ns/rml#source";

    protected final String projectUri;

    public String getProjectUri() {
		return projectUri;
	}

	public BasicPreprocessor(String projectUri) {
        this.projectUri = projectUri;
    }

    /**
     * Gets the source property.
     *
     * @return the rml source property
     */
    protected String getSourceProperty() {
        return RML_SOURCE;
    }

    /**
     * Gets parent node for the source property.
     *
     * @return the parent node
     */
    protected String getSourceParentNode() {
        return RML_RESOURCE;
    }

    @Override
    public Document preprocess(Document doc) {
        doc.setInputFile(preprocessInput(doc));
        if (doc.getInputFile() != null) {
            doc.setMappingFile(preprocessMapping(doc.getMappingFile(), doc.getInputFile()));
        }
        return doc;
    }

    /**
     * The basic preprocessor does not manipulate the input file.
     * This is left to more specialized preprocessors.
     *
     * @param doc the {@link Document}
     * @return the url of the new input file, which is assumed to be on the
     *          systems hard drive or freely accessible in the web.
     */
    protected URL preprocessInput(Document doc) {
        return doc.getInputFile();
    }


    /* load the template mapping and insert the input files to every triples map */
    URL preprocessMapping(URL mapping, URL file) {
        Model partialModel = loadModel(mapping);
        Set<Statement> logicalSources = getStartingNodes(partialModel);
        Property sourceProp = partialModel.createProperty(getSourceProperty());
        Literal sourceLit = partialModel.createLiteral(file.getFile());

        for (Statement logicalSource : logicalSources) {
            partialModel.add(logicalSource.getObject().asResource(), sourceProp, sourceLit);
        }

        /* store the new mapping in a temporary file for the real conversion process */
        try {
            Path tmpFile = Files.createTempFile("mapping", ".ttl");
            RDFDataMgr.write(Files.newOutputStream(tmpFile), partialModel, Lang.TURTLE);
            return tmpFile.toUri().toURL();
        } catch (IOException e) {
            logger.error("Could not rewrite mapping file.");
        }
        return null;
    }


    /* search for all rml parent resources in order to add the input source */
    private Set<Statement> getStartingNodes(Model partialModel) {
        return partialModel.listStatements(new SimpleSelector() {
            @Override
            public boolean test(Statement s) {
                return getSourceParentNode().equals(s.getPredicate().getURI());
            }
        }).toSet();
    }

    /* load rdf models from files on the system drive or the web */
    private Model loadModel(URL model) {
        return RDFDataMgr.loadModel(model.toString());
    }
}
