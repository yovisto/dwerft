package de.werft.tools.importer.rmllib;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.StdRMLEngine;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdRMLMappingFactory;
import be.ugent.mmlab.rml.mapdochandler.retrieval.RMLDocRetrieval;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.importer.rmllib.preprocessing.AlePreprocessor;
import de.werft.tools.importer.rmllib.preprocessing.BasicPreprocessor;
import de.werft.tools.importer.rmllib.preprocessing.CsvPreprocessor;
import de.werft.tools.importer.rmllib.preprocessing.Preprocessor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

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

    public void convert(Document doc) {
        convert(doc, new BasicPreprocessor());
    }

    public void convertCsv(Document doc) {
        convert(doc, new CsvPreprocessor());
    }

    public void convertAle(Document doc) {
        convert(doc, new AlePreprocessor());
    }

    public void convert(Document doc, Preprocessor preprocessor) {
        Document processedDoc = preprocessor.preprocess(doc);
        Model m = RDFDataMgr.loadModel(doc.getMappingFile().getFile());
        RDFDataMgr.write(System.out, m, org.apache.jena.riot.RDFFormat.TURTLE);
        Repository repo = docRetrieval.getMappingDoc(processedDoc.getMappingFile().getFile(), org.openrdf.rio.RDFFormat.TURTLE);
        RMLMapping mapping = mappingFactory.extractRMLMapping(repo);

        RMLDataset dataset = engine.chooseSesameDataSet("dataset", null, null);
        dataset = engine.runRMLMapping(dataset, mapping, "http://example.com", null, null);
        dataset.dumpRDF(System.out, RDFFormat.TURTLE);
    }


}
