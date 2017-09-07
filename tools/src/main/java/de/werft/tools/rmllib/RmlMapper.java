package de.werft.tools.rmllib;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.StdRMLEngine;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdRMLMappingFactory;
import be.ugent.mmlab.rml.mapdochandler.retrieval.RMLDocRetrieval;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.general.Document;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.rmllib.postprocessing.Postprocessor;
import de.werft.tools.rmllib.preprocessing.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.repository.Repository;

/**
 * The RmlMapper takes care of the actual conversion process.
 * It provides access to various preprocessors, as well as your own
 * ones. Then the rml library is utilized to convert the input files.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class RmlMapper {

    private StdRMLMappingFactory mappingFactory;

    private RMLDocRetrieval docRetrieval;

    private RMLEngine engine;

    private DwerftConfig config;

    /**
     * Instantiates a new Rml mapper.
     *
     * @param config the {@link DwerftConfig} for credentials
     */
    public RmlMapper(DwerftConfig config) {
        this.mappingFactory = new StdRMLMappingFactory();
        this.docRetrieval = new RMLDocRetrieval();
        this.engine = new StdRMLEngine();
        this.config = config;
    }

    /**
     * Utilize the {@link BasicPreprocessor} beforehand.
     * The input files are not touched within this method call
     *
     * @param doc the the {@link Document}
     */
    public RMLDataset convertGeneric(Document doc, String projectUri) {
        return convert(doc, new BasicPreprocessor(projectUri));
    }

    /**
     * Utilize the {@link CsvPreprocessor} beforehand.
     *
     * @param doc the the {@link Document}
     */
    public RMLDataset convertCsv(Document doc, String projectUri) {
        return convert(doc, new CsvPreprocessor(projectUri));
    }

    /**
     * Utilize the {@link AlePreprocessor} beforehand.
     *
     * @param doc the the {@link Document}
     */
    public RMLDataset convertAle(Document doc, String projectUri) {
        return convert(doc, new AlePreprocessor(projectUri));
    }

    /**
     * Utilize the {@link AlePreprocessor} beforehand.
     *
     * @param doc the the {@link Document}
     */
    public RMLDataset convertTsv(Document doc, String projectUri) {
        return convert(doc, new TsvPreprocessor(projectUri));
    }

    /**
     * Utilize the {@link PreproducerPreprocessor} beforehand.
     * Since the xml files are fetched from a api no input file is needed.
     *
     * @param doc the {@link Document} without an input file
     */
    public RMLDataset convertPreproducer(Document doc, String projectUri) {
        return convert(doc, new PreproducerPreprocessor(config.getPreProducerKey(),
                config.getPreProducerSecret(), config.getPreProducerAppSecret(), projectUri));
    }

    /**
     * Utilize the {@link DramaqueenPreprocessor} beforehand.
     *
     * @param doc the {@link Document}
     */
    public RMLDataset convertDramaqueen(Document doc, String projectUri) {
        return convert(doc, new DramaqueenPreprocessor(projectUri));
    }

    /**
     * This is the generic preprocessor function which enables
     * a user to provide other preprocessors.
     *
     * @param doc          the {@link Document}
     * @param preprocessor a implementation of {@link Preprocessor}
     */
    public RMLDataset convert(Document doc, Preprocessor preprocessor) {
        return convert(preprocessor.preprocess(doc));
    }

    /**
     * Manipulate the conversion before and after the process to add additional
     * information the the model.
     *
     * @param doc - the {@link Document}
     * @param preprocessor - a implementation of {@link Preprocessor}
     * @param postprocessor - a implementation of {@link Postprocessor}
     * @return a changed {@link RMLDataset}
     */
    public RMLDataset convert(Document doc, Preprocessor preprocessor, Postprocessor postprocessor) {
        RMLDataset dataset = convert(doc, preprocessor);
        return postprocessor.postprocess(dataset, doc);
    }

    /**
     * This convert method is called by all other similar methods.
     * It takes a {@link Document} with a non null output and mapping file.
     * Due to rml specifications the mapping file needs to contain the input files as source triple.
     *
     * @param doc the {@link Document}
     */
    public RMLDataset convert(Document doc) {
        Repository repo = docRetrieval.getMappingDoc(doc.getMappingFile().getFile(), org.openrdf.rio.RDFFormat.TURTLE);
        RMLMapping mapping = mappingFactory.extractRMLMapping(repo);
        RMLDataset dataset = engine.chooseSesameDataSet("dataset", null, null);
        return engine.runRMLMapping(dataset, mapping, "http://example.com", null, null);
    }

    /* helper which shows the actual rml mapping on screen */
    private void showMapping(String location) {
        RDFDataMgr.write(System.out, RDFDataMgr.loadModel(location), Lang.TURTLE);
    }
}
