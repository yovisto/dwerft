package de.werft.tools.rmllib;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.StdRMLEngine;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdRMLMappingFactory;
import be.ugent.mmlab.rml.mapdochandler.retrieval.RMLDocRetrieval;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.rmllib.preprocessing.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

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
     * @param config the {@link DwerftConfig}
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
    public void convertGeneric(Document doc) {
        convert(doc, new BasicPreprocessor());
    }

    /**
     * Utilize the {@link CsvPreprocessor} beforehand.
     *
     * @param doc the the {@link Document}
     */
    public void convertCsv(Document doc) {
        convert(doc, new CsvPreprocessor());
    }

    /**
     * Utilize the {@link AlePreprocessor} beforehand.
     *
     * @param doc the the {@link Document}
     */
    public void convertAle(Document doc) {
        convert(doc, new AlePreprocessor());
    }

    /**
     * Utilize the {@link PreproducerPreprocessor} beforehand.
     * Since the xml files are fetched from a api no input file is needed.
     *
     * @param doc the {@link Document} without an input file
     */
    public void convertPreproducer(Document doc) {
        convert(doc, new PreproducerPreprocessor(config.getPreProducerKey(),
                config.getPreProducerSecret(), config.getPreProducerAppSecret()));
    }

    /**
     * Utilize the {@link DramaqueenPreprocessor} beforehand.
     *
     * @param doc the {@link Document}
     */
    public void convertDramaqueen(Document doc) {
        convert(doc, new DramaqueenPreprocessor());
    }

    /**
     * This is the generic preprocessor function which enables
     * a user to provide other preprocessors.
     *
     * @param doc          the {@link Document}
     * @param preprocessor a implementation of {@link Preprocessor}
     */
    public void convert(Document doc, Preprocessor preprocessor) {
        convert(preprocessor.preprocess(doc));
    }

    /**
     * This convert method is called by all other similar methods.
     * It takes a {@link Document} with a non null output and mapping file.
     * Due to rml specifications the mapping file needs to contain the input files as source triple.
     *
     * @param doc the {@link Document}
     */
    public void convert(Document doc) {
        Repository repo = docRetrieval.getMappingDoc(doc.getMappingFile().getFile(), org.openrdf.rio.RDFFormat.TURTLE);
        RMLMapping mapping = mappingFactory.extractRMLMapping(repo);
        RMLDataset dataset = engine.chooseSesameDataSet("dataset", null, null);
        dataset = engine.runRMLMapping(dataset, mapping, "http://example.com", null, null);

        try {
            writeResult(dataset, doc.getOutputFile());
        } catch (FileNotFoundException e) {
            //TODO tinylogger
            e.printStackTrace();
        }
    }

    /* dump the dataset to a file */
    private void writeResult(RMLDataset dataset, URL out) throws FileNotFoundException {
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(out.getFile()));
        dataset.dumpRDF(stream, RDFFormat.TURTLE);
    }

    /* helper which shows the actual rml mapping on screen */
    private void showMapping(String location) {
        RDFDataMgr.write(System.out, RDFDataMgr.loadModel(location), Lang.TURTLE);
    }

    /* helper which prints the dataset on screen with jena, because openrdf doesn't pretty print */
    private void showResult(RMLDataset dataset) {
        try {
            dataset.dumpRDF(new FileOutputStream("/tmp/file.ttl"), RDFFormat.TURTLE);
        } catch (FileNotFoundException e) {
            //TODO tinylogger
            e.printStackTrace();
        }
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, "/tmp/file.ttl");
        m.setNsPrefix("for", "http://filmontology.org/resource/");
        m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");
        RDFDataMgr.write(System.out, m, Lang.TTL);
    }


}
