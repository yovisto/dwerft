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
 * Created by ratzeputz on 02.09.16.
 */
public class RmlMapper {

    private StdRMLMappingFactory mappingFactory;

    private RMLDocRetrieval docRetrieval;

    private RMLEngine engine;

    private DwerftConfig config;

    public RmlMapper(DwerftConfig config) {
        this.mappingFactory = new StdRMLMappingFactory();
        this.docRetrieval = new RMLDocRetrieval();
        this.engine = new StdRMLEngine();
        this.config = config;
    }

    public void convertGeneric(Document doc) {
        convert(doc, new BasicPreprocessor());
    }

    public void convertCsv(Document doc) {
        convert(doc, new CsvPreprocessor());
    }

    public void convertAle(Document doc) {
        convert(doc, new AlePreprocessor());
    }

    public void convert(Document doc, Preprocessor preprocessor) {
        convert(preprocessor.preprocess(doc));
    }

    public void convertPreproducer(Document doc) {
        convert(doc, new PreproducerPreprocessor(config.getPreProducerKey(),
                config.getPreProducerSecret(), config.getPreProducerAppSecret()));
    }

    public void convertDramaqueen(Document doc) {
        convert(doc, new DramaqueenPreprocessor());
    }

    public void convert(Document doc) {

        //showMapping(doc.getMappingFile().getFile());
        Repository repo = docRetrieval.getMappingDoc(doc.getMappingFile().getFile(), org.openrdf.rio.RDFFormat.TURTLE);
        RMLMapping mapping = mappingFactory.extractRMLMapping(repo);

        RMLDataset dataset = engine.chooseSesameDataSet("dataset", null, null);
        dataset = engine.runRMLMapping(dataset, mapping, "http://example.com", null, null);
        showResult(dataset);
    }

    /* show the actual rml mapping on screen */
    private void showMapping(String location) {
        RDFDataMgr.write(System.out, RDFDataMgr.loadModel(location), Lang.TURTLE);
    }

    /* print the dataset on screen with jena, because openrdf doesn't pretty print */
    private void showResult(RMLDataset dataset) {
        try {
            dataset.dumpRDF(new FileOutputStream("/tmp/file.ttl"), RDFFormat.TURTLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, "/tmp/file.ttl");
        m.setNsPrefix("for", "http://filmontology.org/resource/");
        m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");
        //m.write(, Lang.TURTLE);
        RDFDataMgr.write(System.out, m, Lang.TTL);
    }

    /* dump the dataset to a file */
    private void writeResult(RMLDataset dataset, URL out) throws FileNotFoundException {
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(out.getFile()));
        dataset.dumpRDF(stream, RDFFormat.TURTLE);
    }
}
