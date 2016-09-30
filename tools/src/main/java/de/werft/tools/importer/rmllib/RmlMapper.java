package de.werft.tools.importer.rmllib;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.StdRMLEngine;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdRMLMappingFactory;
import be.ugent.mmlab.rml.mapdochandler.retrieval.RMLDocRetrieval;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.importer.rmllib.preprocessing.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by ratzeputz on 02.09.16.
 */
public class RmlMapper {

    private StdRMLMappingFactory mappingFactory;

    private RMLDocRetrieval docRetrieval;

    private RMLEngine engine;

    public RmlMapper() {
        mappingFactory = new StdRMLMappingFactory();
        docRetrieval = new RMLDocRetrieval();
        engine = new StdRMLEngine();
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
        convert(doc, new PreproducerPreprocessor("19122", "uiW41KYR", "i8qxC45N"));
    }

    public void convert(Document doc) {

        //showMapping(doc.getMappingFile().getFile());
        Repository repo = docRetrieval.getMappingDoc(doc.getMappingFile().getFile(), org.openrdf.rio.RDFFormat.TURTLE);
        RMLMapping mapping = mappingFactory.extractRMLMapping(repo);

        RMLDataset dataset = engine.chooseSesameDataSet("dataset", null, null);
        dataset = engine.runRMLMapping(dataset, mapping, "http://example.com", null, null);
        showResult(dataset);
    }

    private void showMapping(String location) {
        RDFDataMgr.write(System.out, RDFDataMgr.loadModel(location), Lang.TURTLE);
    }

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

}
