package de.werft.tools.importer.rmllib.preprocessing;

import de.werft.tools.importer.rmllib.Document;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ratzeputz on 09.09.16.
 */
public class BasicPreprocessor implements Preprocessor {


    private static final String RML_RESOURCE = "http://semweb.mmlab.be/ns/rml#logicalSource";

    private static final String RML_SOURCE = "http://semweb.mmlab.be/ns/rml#source";

    protected String getProperty() {
        return RML_SOURCE;
    }

    protected String getParentNode() {
        return RML_RESOURCE;
    }

    @Override
    public Document preprocess(Document doc) {
        doc.setInputFile(preprocessInput(doc));

        if (doc.getInputFile() != null) {
            doc.setMappingFile(preprocessMapping(doc.getMappingFile(), doc.getInputFile()));
        }
;
        return doc;
    }

    protected URL preprocessInput(Document doc) {
        return doc.getInputFile();
    }


    private URL preprocessMapping(URL mapping, URL file) {
        Model partialModel = loadModel(mapping);
        Set<Resource> bnodes = getStartingNodes(partialModel);
        partialModel.add(bnodes.iterator().next(), partialModel.createProperty(getProperty()), partialModel.createLiteral(file.getFile()));

        try {
            Path tmpFile = Files.createTempFile("blah", ".ttl");
            RDFDataMgr.write(Files.newOutputStream(tmpFile), partialModel, Lang.TURTLE);
            return tmpFile.toUri().toURL();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Set<Resource> getStartingNodes(Model partialModel) {
        Set<Resource> headResources = new HashSet<>();
        StmtIterator itr = partialModel.listStatements();

        while (itr.hasNext()) {
            Statement stmt = itr.nextStatement();

            if (getParentNode().equals(stmt.getPredicate().getURI())) {
                headResources.add(stmt.getObject().asResource());
            }
        }

        return headResources;
    }

    private org.apache.jena.rdf.model.Model loadModel(URL mapping) {
        return RDFDataMgr.loadModel(mapping.toString());
    }
}
