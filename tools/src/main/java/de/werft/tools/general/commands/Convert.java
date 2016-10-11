package de.werft.tools.general.commands;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.general.DwerftTools;
import de.werft.tools.rmllib.Document;
import de.werft.tools.rmllib.RmlMapper;
import de.werft.tools.rmllib.preprocessing.*;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The convert command provides access to the rml library responsible for
 * converting different types of structured input to rdf with the help of
 * an rml mapping file.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Command(name="convert",description="Convert XML, CSV, ALE, JSON files to RDF.")
public class Convert extends DwerftTools {

    @Arguments(description = "Starts conversion process. Based on the file extension we determine" +
            " which converter is used.\n Available inputs are *.dp for dramaqueen; *.ale for ALE; *.csv for csv; *.xml for Generic;" +
            " no input for preproducer.\n Available outputs are no output for csv, ale to xml conversion and *.(rdf|ttl|nt) for everything else.\n" +
            " Provide a mapping only for generic conversion. " +
            " Usage: [<input>] [<output>] [<mapping>]")
    private List<String> files = new ArrayList<>(10);

    @Option(name = {"-f", "--format"}, description = "Specifies rdf output format. Available options are " +
            "Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
    private String format = "ttl";

    @Option(name = {"-p", "--print"}, description = "Print conversion output to console instead of file.")
    private boolean print = false;

    private final String RDF_SUFFIX = "(rdf|ttl|n3|nt)";

    @Override
    public void run() {
        logger.debug("Convert files " + files);

        try {
            Preprocessor p = choosePreprocessor();
            logger.debug("Choosen preprocessor: " + p);
            Document d = new Document(getMappingFile(), getInputFile(), getOutputFile());
            logger.debug("Build document: " + d);
            verifyChoosing(d, p);

            RmlMapper mapper = new RmlMapper(config);
            RMLDataset dataset = mapper.convert(d, p);
            writeResult(dataset, d.getOutputFile(), format);

        } catch (InstantiationException e) {
            logger.error("Instantiation failed. " + e.getMessage());
        } catch (MalformedURLException e) {
            logger.error("Could not convert given files to urls. " + e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("Could present results: " + e.getMessage());
        }
    }

    /* check some constraints */
    private void verifyChoosing(Document d, Preprocessor p) throws InstantiationException {
        /* the input can only be empty if we use preprocessor */
        if (d.getInputFile() == null && !(p instanceof PreproducerPreprocessor)) {
            throw new InstantiationException("No input provided and input tool is not preproducer.");
        } else if (isEmptyKeys(config) && p instanceof PreproducerPreprocessor) {
            throw new InstantiationException("No preproducer credentials given.");
        } else if (d.getOutputFile() == null && !print) {
            throw new InstantiationException("Need a valid output file if printing to screen is not set.");
        }
    }

    /* How to choose the preprocessor
            print       no print
    Prep    0           1(rdf)
    Dq      1(dq)       2(dq|rdf)
    ALE     2(ale|rml)  3(ale|rdf|rml)
    CSV     2(csv|rml)  3(csv|rdf|rml)
    XML     2(xml|rml)  3(xml|rdf|rml)
    JSON    2(json|rml) 3(json|rdf|rml)
     */
    private Preprocessor choosePreprocessor() throws InstantiationException {
        /* we choose preproducer as input tool */
        if (files.size() == 0 || (files.size() == 1 && hasExtension(files.get(0), RDF_SUFFIX))) {
            return new PreproducerPreprocessor(config.getPreProducerKey(), config.getPreProducerSecret(), config.getPreProducerAppSecret());
        } else if (hasExtension(files.get(0), "dq")) {
            return new DramaqueenPreprocessor();
        } else if (hasExtension(files.get(0), "ale")) {
            return new AlePreprocessor();
        } else if (hasExtension(files.get(0), "csv")) {
            return new CsvPreprocessor();
        } else if (hasExtension(files.get(0), "(xml|json)")) {
            return new BasicPreprocessor();
        } else {
            throw new InstantiationException("Failed to choose the correct preprocessor. Check the file order.");
        }
    }

    /* choose the first input file if we have one */
    private URL getInputFile() throws MalformedURLException {
        if (files.size() != 0 && !hasExtension(files.get(0), RDF_SUFFIX)) {
            /* check that we have no preproducer input */
            return new File(files.get(0)).toURI().toURL();
        }
        return null;
    }

    /* return output file if is not print to cli */
    private URL getOutputFile() throws MalformedURLException {
        if (!print && files.size() > 1) {
            return new File(files.get(1)).toURI().toURL();
        } else if (files.size() == 1 && !print) {
            /* we have preproducer with normal output */
            return new File(files.get(0)).toURI().toURL();
        }
        return null;
    }

    /* return mapping file and handle special cases where we provide the mappings */
    private URL getMappingFile() throws MalformedURLException, InstantiationException {
        if (print && files.size() == 2) {
            return new File(files.get(1)).toURI().toURL();
        } else if (!print && files.size() == 3){
            return new File(files.get(2)).toURI().toURL();
        } else if (files.size() == 0 || (files.size() == 1 && hasExtension(files.get(0), RDF_SUFFIX))) {
            /* get the preproducer mapping */
            return determineMappingFile(config.getPreProducerMappingName(), config.getMappingFolder()).toURI().toURL();
        } else if (hasExtension(files.get(0), "dq")) {
            /* get the dramaqueen mapping */
            return determineMappingFile(config.getDramaQueenMappingName(), config.getMappingFolder()).toURI().toURL();
        }
        throw new InstantiationException("Could not find an appropriate mapping file.");
    }


    /* Checks weather all credentials are set in the config file or not. */
    private static boolean isEmptyKeys(DwerftConfig config) {
        return StringUtils.isEmpty(config.getPreProducerAppSecret()) || StringUtils.isEmpty(config.getPreProducerKey())
                || StringUtils.isEmpty(config.getPreProducerSecret());
    }

    /* test if a file has a certain extension */
    private boolean hasExtension(String file, String extensions) {
        return StringUtils.substringAfterLast(file, ".").toLowerCase().matches(extensions);
    }

    /* choose the mapping from classpath or external folder */
    private File determineMappingFile(String file, File internalFolder) {
        File searchFile = new File(file);
        if (searchFile.isFile()) {
            return searchFile;
        }

        for (File f : internalFolder.listFiles()) {
            if (StringUtils.equalsIgnoreCase(file, f.getName())) {
                searchFile = f; // inside classpath
            }
        }
        return searchFile;
    }

    /* dump the dataset to a file or on screen */
    private void writeResult(RMLDataset dataset, URL out, String format) throws FileNotFoundException {
        dataset.dumpRDF(new FileOutputStream("/tmp/file.ttl"), RDFFormat.TURTLE);

        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, "/tmp/file.ttl");
        m.setNsPrefix("for", "http://filmontology.org/resource/");
        m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");

        if (print) {
            RDFDataMgr.write(System.out, m, RDFLanguages.nameToLang(format));
        } else {
            RDFDataMgr.write(new FileOutputStream(out.getFile()), m, RDFLanguages.nameToLang(format));
        }
    }
}