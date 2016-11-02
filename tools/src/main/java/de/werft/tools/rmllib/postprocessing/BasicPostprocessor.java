package de.werft.tools.rmllib.postprocessing;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import be.ugent.mmlab.rml.model.dataset.StdRMLDataset;
import de.werft.tools.general.Document;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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


    /**
     * Override this method to post process the rdf model.
     *
     * @param model - the rdf jena model
     * @param doc - the document
     * @return a changed model
     */
    protected Model process(Model model, Document doc) {
        return model;
    }
}
