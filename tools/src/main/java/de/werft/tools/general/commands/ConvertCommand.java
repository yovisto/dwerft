package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.general.OntologyConstants;
import de.werft.tools.importer.csv.AleToXmlConverter;
import de.werft.tools.importer.csv.CsvToXmlConverter;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdf;
import de.werft.tools.importer.general.Converter;
import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;
import de.werft.tools.importer.preproducer.PreProducerToRdf;
import de.werft.tools.sources.PreproducerSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            return hasExtension(files.get(0), "(ale|csv|rdf|nt|ttl)");
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
    public Converter getConverter(DwerftConfig conf) throws InstantiationException {
        Converter c;
        File mappingFolder = conf.getMappingFolder();

        if (isCsvToXml()) { // csv to xml
            c = getCsvConverter(getInput());

        } else if (isConvertPreproducer()) { // preproducer to rdf
            if (isEmptyKeys(conf)) {
                throw new InstantiationException("No PreProducer credentials found.");
            }
            String mappingFile = determineMappingFile(conf.getPreProducerMappingName(), mappingFolder);
            PreproducerSource pps = new PreproducerSource(conf.getPreProducerKey(),
                    conf.getPreProducerSecret(), conf.getPreProducerAppSecret());

            c = new PreProducerToRdf(
                    OntologyConstants.ONTOLOGY_FILE,
                    OntologyConstants.ONTOLOGY_FORMAT,
                    mappingFile, pps);

        } else if (isConvertDramaqueen()) { // dramaqueen to rdf
            String mappingFile = determineMappingFile(conf.getDramaQueenMappingName(), mappingFolder);
            c = new DramaqueenToRdf(
                    OntologyConstants.ONTOLOGY_FILE,
                    OntologyConstants.ONTOLOGY_FORMAT,
                    mappingFile);

        } else if (isCsvToRdf()) { // csv to rdf
            String mappingFile = determineMappingFile(getMapping(), mappingFolder);
            c = new DefaultXMLtoRDFconverter(
                    OntologyConstants.ONTOLOGY_FILE,
                    OntologyConstants.ONTOLOGY_FORMAT,
                    mappingFile);
            c.setPreConverter(getCsvConverter(getInput()));

        } else if (isConvertGeneric()) { // generic conversion
            String mappingFile = determineMappingFile(getMapping(), mappingFolder);
            c = new DefaultXMLtoRDFconverter(
                    OntologyConstants.ONTOLOGY_FILE,
                    OntologyConstants.ONTOLOGY_FORMAT,
                    mappingFile);
        } else {
            throw new InstantiationException("Failed to choose the correct converter.");
        }

        return c;
    }

    private Converter<File> getCsvConverter(String input) throws InstantiationException {
        Converter<File> c = null;
        try {
            if (hasExtension(input, "csv")) {
                c = new CsvToXmlConverter(';');
            } else {
                c = new AleToXmlConverter('\t');
            }
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            throw new InstantiationException("Failed to create CSV or ALE converter. " + e.getMessage());
        }
        return c;
    }

    // here happens the guesssing magic based on file endings and the amount of provided files
    private boolean isConvertDramaqueen() {
        return files.size() == 2 && hasExtension(files.get(0), "dq");
    }

    private boolean isCsvToXml() {
        return files.size() == 1 && hasExtension(files.get(0), "(ale|csv)");
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

    // Checks weather all credentials are set in the config file or not.
    private static boolean isEmptyKeys(DwerftConfig config) {
        return StringUtils.isEmpty(config.getPreProducerAppSecret()) || StringUtils.isEmpty(config.getPreProducerKey())
                || StringUtils.isEmpty(config.getPreProducerSecret());
    }

    // test if a file has a certain extension
    private boolean hasExtension(String file, String extensions) {
        return StringUtils.substringAfterLast(file, ".").toLowerCase().matches(extensions);
    }

    // choose the mapping from classpath or external folder
    private String determineMappingFile(String file, File internalFolder) {
        File[] mappings = internalFolder.listFiles();

        for (int i = 0; i < mappings.length; i++) {
            if (StringUtils.equalsIgnoreCase(file, mappings[i].getName())) {
                return mappings[i].getAbsolutePath(); // inside classpath
            }
        }
        return file;
    }
}
