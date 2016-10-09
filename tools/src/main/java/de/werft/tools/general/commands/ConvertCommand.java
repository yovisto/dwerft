package de.werft.tools.general.commands;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.rmllib.Document;
import de.werft.tools.rmllib.RmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.openrdf.rio.RDFFormat;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.jena.vocabulary.LocationMappingVocab.mapping;

/**
 * This class specifies the convert sub command used by the
 * dwerft tools. It takes care of converting format string to
 * {@link Lang} and is responsible for guessing the correct converter to use.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Converts XML, CSV, ALE files to RDF.")
public class ConvertCommand {

    // main commands
    @Parameter(variableArity = true, required=true, description = "Starts conversion process. Based on the file extension we determine" +
            " which converter is used.\n Available inputs are *.dp for dramaqueen; *.ale for ALE; *.csv for csv; *.xml for Generic;" +
            " no input for preproducer.\n Available outputs are no output for csv, ale to xml conversion and *.(rdf|ttl|nt) for everything else.\n" +
            " Provide a mapping only for generic conversion. " +
            " Usage: [<input>] <output> [<mapping>]")
    private List<String> files = new ArrayList<>();

    // optional commands
    @Parameter(names = {"-format"}, /*converter = LangConverter.class,*/ arity = 1, description = "Specifies rdf output format. " +
            "Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
    private String format = "ttl";

    @Parameter(names = {"-print"}, description = "Print conversion output to console instead of file.")
    private boolean printToCli = false;

    /**
     * Gets {@link Lang} output format.
     *
     * @return the format
     */
    public Lang getFormat() {
        Lang resultFormat = RDFLanguages.nameToLang(format.toUpperCase());
        // no language found for the specified format
        if (resultFormat == null) {
            resultFormat = Lang.TTL;
        }
        return resultFormat;
    }

    /**
     * If the result is printed to cli instead of a file
     *
     * @return the boolean
     */
    public boolean isPrintToCli() {
        return printToCli;
    }

    /**
     * If we have an incorrect amount of files
     *
     * @return true if the amount is incorrect
     */
    public boolean hasIncorrectFilesCount() {
        return files.size() > 3 || files.size() < 1;
    }

    /**
     * If the files are in the correct order.
     * This is determined, by the possible file endings
     *
     * @return true if the files are in the right order
     */
    public boolean isCorrectFileOrder() {
        if (files.size() == 1) {
            return hasExtension(files.get(0), "(rdf|nt|ttl)");
        } else if (files.size() == 2 || files.size() == 3) {
            return hasExtension(files.get(0), "(dq|xml|ale|csv)") &&
                    hasExtension(files.get(1), "(rdf|nt|ttl)");
        } else {
            return false;
        }
    }

    /**
     * Gets input file for conversion.
     *
     * @return the input file
     */
    public String getInput() {
        if (isConvertPreproducer()) {
            return "";
        } else {
            return files.get(0);
        }
    }

    /**
     * Gets the output file for conversion.
     *
     * @return the output file
     */
    public String getOutput() {
        if (isConvertPreproducer()) {
            return files.get(0);
        } else {
            return files.get(1);
        }
    }

    /**
     * Get the converters determined for the task.
     * Most of the times one converter is returned but if
     * the conversion is from csv to rdf two converters are returned.
     * The result relays heavily on guessing the correct converter from
     * the given input files. Check if the file ordering is correct with
     * {@link ConvertCommand#isCorrectFileOrder} beforehand.
     *
     * @param conf the general configuration, see {@link DwerftConfig}
     * @return the array of converters, most of the times with only one element
     * @throws InstantiationException the instantiation exception
     */
    public void convert(DwerftConfig conf) throws InstantiationException, IOException {
        RMLDataset dataset;
        RmlMapper mapper = new RmlMapper(conf);
        File mappingFolder = conf.getMappingFolder();
        Document doc = new Document(new File(getMapping()).toURI().toURL(),
                new File(getInput()).toURI().toURL(),
                new File (getOutput()).toURI().toURL());


        if (isConvertPreproducer()) { // preproducer to rdf
            if (isEmptyKeys(conf)) {
                throw new InstantiationException("No PreProducer credentials found.");
            }

            File mappingFile = determineMappingFile(conf.getPreProducerMappingName(), mappingFolder);
            doc.setMappingFile(mappingFile.toURI().toURL());
            dataset = mapper.convertPreproducer(doc);

        } else if (isConvertDramaqueen()) { // dramaqueen to rdf
            File mappingFile = determineMappingFile(conf.getDramaQueenMappingName(), mappingFolder);
            doc.setMappingFile(mappingFile.toURI().toURL());
            dataset = mapper.convertDramaqueen(doc);

        } else if (isCsvToRdf()) { // csv to rdf
            File mappingFile = determineMappingFile(getMapping(), mappingFolder);
            doc.setMappingFile(mappingFile.toURI().toURL());
            dataset = convertCsv(mapper, doc);

        } else if (isConvertGeneric()) { // generic conversion
            File mappingFile = determineMappingFile(getMapping(), mappingFolder);
            doc.setMappingFile(mappingFile.toURI().toURL());
            dataset = mapper.convertGeneric(doc);
        } else {
            throw new InstantiationException("Failed to choose the correct converter.");
        }

        if (isPrintToCli()) {
            showResult(dataset);
        } else {
            try {
                writeResult(dataset, doc.getOutputFile());
            } catch (FileNotFoundException e) {
                throw new IOException("Result could not be written to output file.", e);
            }
        }
    }

    private RMLDataset convertCsv(RmlMapper mapper, Document doc) throws InstantiationException {
        return hasExtension(getInput(), "csv") ? mapper.convertCsv(doc) : mapper.convertAle(doc);
    }

    // here happens the guesssing magic based on file endings and the amount of provided files
    private boolean isConvertDramaqueen() {
        return files.size() == 2 && hasExtension(files.get(0), "dq");
    }

    private boolean isConvertPreproducer() {
        return files.size() == 1 && hasExtension(files.get(0), "(rdf|nt|ttl)");
    }

    private boolean isCsvToRdf() {
        return files.size() == 3 && hasExtension(files.get(0), "(ale|csv)");
    }

    private boolean isConvertGeneric() {
        return files.size() == 3 && hasExtension(files.get(0), "xml");
    }

    private String getMapping() {
        if (isCsvToRdf() || isConvertGeneric()) {
            return files.get(2);
        } else {
            return "";
        }
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
        File[] mappings = internalFolder.listFiles();

        for (int i = 0; mapping == null || i < mappings.length; i++) {
            if (StringUtils.equalsIgnoreCase(file, mappings[i].getName())) {
                searchFile = mappings[i]; // inside classpath
            }
        }
        return searchFile;
    }

    /* dump the dataset to a file */
    private void writeResult(RMLDataset dataset, URL out) throws FileNotFoundException {
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(out.getFile()));
        dataset.dumpRDF(stream, RDFFormat.TURTLE);
    }

    /* helper which prints the dataset on screen with jena, because openrdf doesn't pretty print */
    private void showResult(RMLDataset dataset) throws FileNotFoundException {
        dataset.dumpRDF(new FileOutputStream("/tmp/file.ttl"), RDFFormat.TURTLE);

        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, "/tmp/file.ttl");
        m.setNsPrefix("for", "http://filmontology.org/resource/");
        m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");
        RDFDataMgr.write(System.out, m, Lang.TTL);
    }
}
