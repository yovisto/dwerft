package de.werft.tools.general.commands;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.ProseSection;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.werft.tools.general.Document;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.general.DwerftTools;
import de.werft.tools.rmllib.RmlMapper;
import de.werft.tools.rmllib.postprocessing.*;
import de.werft.tools.rmllib.preprocessing.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
@ProseSection(title = "Additional Information",
    paragraphs = {"This command is used to start the conversion process." +
        "The used conversion procedure is determined through the file suffixes and ordering." +
        "Available inputs are *.ale, *.csv, *.xml, *.json, *.dq and nothing for Preproducer." +
        "Available outputs are everything provided by Jena (ttl, n3, rdf/xml, nt ...), but the file suffixes are limited to",
        "*.rdf, *.ttl, *.n3, *.nt ."})
public class Convert extends DwerftTools {

    @Arguments(description = "input, output and mapping file for the conversion process.",
    title = {"<input>", "<output>", "<mapping>"})
    private List<String> files = new ArrayList<>(10);

    @Option(name = {"-f", "--format"}, description = "Specifies rdf output format. Available options are " +
            "all provided by Jena. Default is Turtle.")
    private String format = "ttl";

    @Option(name = {"-p", "--print"}, description = "Print conversion output to console instead of file.")
    private boolean print = false;

    @Option(name = {"-u", "--project-uri"}, description = "A uri which refers to the main project.")
    @Required
    private String projectUri = "";

    @Option(name = {"-s", "--split"}, description = "If CSV files contains sub lists, these can be splitted and the rows" +
            "are duplicated. Provide column position number starting with 0.")
    private int splitCol = -1;

    private final String RDF_SUFFIX = "(rdf|ttl|n3|nt)";

    @Override
    public void run() {
        super.run();
        logger.debug("Convert files " + files);

        try {
            Preprocessor pre = choosePreprocessor();
            Postprocessor post = choosePostprocessor();
            logger.debug("Choosen preprocessor: " + pre);
            logger.debug("Choosen postprocessor: " + post);
            Document d = new Document(getMappingFile(), getInputFile(), getOutputFile());
            logger.debug("Build document: " + d);
            verifyChoosing(d, pre);

            RmlMapper mapper = new RmlMapper(config);
            RMLDataset dataset = mapper.convert(d, pre, post);
            writeResult(dataset, d.getOutputFile(), format, config.getOutputPrefixes());

            logger.info("Successfully converted " + d.getInputFile() + " to rdf " + d.getOutputFile());
        } catch (InstantiationException e) {
            logger.error("Instantiation failed. " + e.getMessage());
        } catch (MalformedURLException e) {
            logger.error("Could not convert given files to urls. " + e.getMessage());
        } catch (IOException e) {
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
    Tsv     2(tsv|rml)  3(tsv|rdf|rml)
    ALE     2(ale|rml)  3(ale|rdf|rml)
    CSV     2(csv|rml)  3(csv|rdf|rml)
    XML     2(xml|rml)  3(xml|rdf|rml)
    JSON    2(json|rml) 3(json|rdf|rml)
     */
    private Preprocessor choosePreprocessor() throws InstantiationException {
        /* we choose preproducer as input tool */
        if (files.size() == 0 || (files.size() == 1 && hasExtension(files.get(0), RDF_SUFFIX))) {
            return new PreproducerPreprocessor(config.getPreProducerKey(), config.getPreProducerSecret(), config.getPreProducerAppSecret(), projectUri);
        } else if (hasExtension(files.get(0), "dq")) {
            return new DramaqueenPreprocessor(projectUri);
        } else if (hasExtension(files.get(0), "ale")) {
            return new AlePreprocessor(projectUri);
        } else if (hasExtension(files.get(0), "tsv")) {
            return new TsvPreprocessor(projectUri, splitCol);
        } else if (hasExtension(files.get(0), "csv")) {
            return new CsvPreprocessor(projectUri);
        } else if (hasExtension(files.get(0), "(xml|json)")) {
            return new BasicPreprocessor(projectUri);
        } else {
            throw new InstantiationException("Failed to choose the correct preprocessor. Check the file order.");
        }
    }

    private Postprocessor choosePostprocessor() throws InstantiationException {
        /* at the moment there is only dq which needs post processing */
        if (files.size() > 0 && hasExtension(files.get(0), "dq")) {
            return new DramaqueenPostprocessor(projectUri);
        } else if (files.size() == 0 || (files.size() == 1 && hasExtension(files.get(0), RDF_SUFFIX))) {
        	return new PreproducerPostprocessor(projectUri);
        } else if (hasExtension(files.get(0), "ale")) {
            return new AlePostprocessor(projectUri);
        } else {
            return new BasicPostprocessor(projectUri);
        }
    }

    /* choose the first input file if we have one */
    private URL getInputFile() throws MalformedURLException, InstantiationException {
        if (files.size() != 0 && !hasExtension(files.get(0), RDF_SUFFIX)) {
            /* check that we have no preproducer input and the input file exists */
            if (!new File(files.get(0)).exists()) {
                throw new InstantiationException("The input file is not found on disk");
            }

            return new File(files.get(0)).toURI().toURL();
        }
        return null;
    }

    /* return output file if is not print to cli */
    private URL getOutputFile() throws MalformedURLException {
        if (!print && files.size() > 1) {
            return new File(files.get(1)).toURI().toURL();
        } else if (files.size() == 1 && !print && !hasExtension(files.get(0), "dq") ) {
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
        } else if (hasExtension(files.get(0), "ale")) {
            /* get the ale mapping */
            return determineMappingFile(config.getAleMappingName(), config.getMappingFolder()).toURI().toURL();
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
    private void writeResult(RMLDataset dataset, URL out, String format, List<String> prefixes) throws IOException {
    	Path tmp = Files.createTempFile("tmp", ".ttl");
    	dataset.dumpRDF(new FileOutputStream(tmp.toFile()), RDFFormat.TURTLE);

        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, tmp.toUri().toString());

        for (String prefix : prefixes) {
            String akr = org.apache.commons.lang.StringUtils.substringBefore(prefix, ":");
            String full_uri = org.apache.commons.lang.StringUtils.substringAfter(prefix, ":");
            m.setNsPrefix(akr, full_uri);
        }

        if (print) {
            RDFDataMgr.write(System.out, m, RDFLanguages.nameToLang(format));
        } else {
            RDFDataMgr.write(new FileOutputStream(out.getFile()), m, RDFLanguages.nameToLang(format));
        }
    }
}