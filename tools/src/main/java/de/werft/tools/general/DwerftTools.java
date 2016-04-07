package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.werft.tools.importer.csv.AleToXmlConverter;
import de.werft.tools.importer.csv.CsvToXmlConverter;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdf;
import de.werft.tools.importer.general.AbstractXMLtoRDFconverter;
import de.werft.tools.importer.preproducer.PreProducerToRdf;
import de.werft.tools.sources.AbstractSource;
import de.werft.tools.sources.DramaQueenSource;
import de.werft.tools.sources.PreproducerSource;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

    /** The Logger. */
    private static final Logger L = Logger.getLogger(DwerftTools.class.getName());

    // main parameters
    @Parameter(names = {"-convert"}, variableArity = true, description = "Starts conversion process. Based on the file extension we determine" +
            " which converter is used. Available inputs are *.dp for dramaqueen; *.ale for ALE; *.csv for csv; *.xml for Generic;" +
            " no input for preproducer. Available outputs are no output for csv, ale to xml conversion and *.(rdf|ttl|nt) for everything else." +
            " Provide a mapping only for generic conversion. " +
            " Usage: [<input>] <output> [<mapping>]")
    private List<String> files = new ArrayList<>();

    @Parameter(names = {"-upload"}, arity = 1, description = "Uploads a file to a specified sparql endpoint. Valid formats are" +
            " *.(rdf|ttl|nt|jsonld)")
    private String uploadFile = "";

    // optional parameters
    @Parameter(names = {"-format"}, arity = 1, description = "Specifies rdf output format. " +
            "Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
    private String format = "ttl";

    @Parameter(names = {"-help"}, help = true, description = "Shows this help message.")
    private boolean isHelp = false;

    @Parameter(names = {"-print"}, description = "Print conversion output to console instead of file.")
    private boolean printToCli = false;

    private DwerftConfig config;

    public DwerftTools() throws InvalidKeyException {
        this.config = ConfigFactory.create(DwerftConfig.class);
        OntologyConstants.setOntologyFile(config.getOntologyFile());
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        //Configure log4j2
        BasicConfigurator.configure();
        //L.error("Work:" + System.getProperty("user.dir"));
        try {
            DwerftTools tools = new DwerftTools();
            tools.run(args);
        } catch (InvalidKeyException e1) {
            L.error("Wrong keys configured. " + e1.getMessage());
        }
    }

    /**
     * Main entry point
     *
     * @param args the program arguments
     */
    public void run(String[] args) throws InvalidKeyException {
        //Parse cli arguments
        JCommander cmd = new JCommander(this);
        cmd.parse(args);

        if (isHelp) {
            cmd.usage();
            System.exit(0);
        }

        if (!uploadFile.isEmpty() && files.isEmpty()) {
            upload(cmd);
        } else if (!files.isEmpty() && uploadFile.isEmpty()) {
            convert(cmd);
        } else {
            beatUser(cmd);
        }

        System.exit(0);
    }

    // this method does the actual conversion with some guessing magic based on file extensions and file count
    private void convert(JCommander cmd) throws InvalidKeyException {
        if (files.size() > 3 && files.size() < 1) {
            beatUser(cmd);
        }

        AbstractXMLtoRDFconverter converter = null;
        String input = files.get(0);
        String output = files.get(0);
        if (files.size() == 1 && hasExtension(input, "(ale|csv)")) { // csv to xml
            File f = convertCsvToXml(input);
            L.info("Converted " + input + " to " + f);
        } else if (files.size() == 1 && hasExtension(output, "(rdf|nt|ttl)")) { // preproducer to rdf
            String mappingFile = determineMappingFile("preproducer.mappings", config.getMappingFolder());
            converter = prpToRdf(config, input);

        } else {
            // FIXME fix this messy workaround
            String secondOutput = files.get(1);
            if (!hasExtension(secondOutput, "(rdf|nt|ttl)")) {
                beatUser(cmd);
            } else {
                output = secondOutput;
            }

            if (hasExtension(input, "dq") && files.size() == 2) { // dramaqueen to rdf
                String mappingFile = determineMappingFile("dramaqueen.mappings", config.getMappingFolder());
                converter = dqToRdf(input, mappingFile);

            } else if (hasExtension(input, "(ale|csv)") && files.size() == 3) { // csv to rdf
                String file = convertCsvToXml(input).getAbsolutePath();
                L.info("Converted " + input + " to " + file);
                String mappingFile = determineMappingFile(files.get(2), config.getMappingFolder());
                converter = genericXmlToRdf(file, mappingFile);

            } else if (hasExtension(input, "xml") && files.size() == 3) { // generic conversion
                String mappingFile = determineMappingFile(files.get(2), config.getMappingFolder());
                converter = genericXmlToRdf(input, mappingFile);

            } else {
                beatUser(cmd);
            }

        }

        // write to cli or fs
        if (converter != null) {
            if (printToCli) {
                converter.writeRdfToConsole(getFormat(format));
            } else {
                converter.writeRdfToFile(output, getFormat(format));
            }
            L.info("File " + input + " converted to " + output + " successfully.");
        }
    }

    private boolean upload(JCommander cmd) {
        if (hasExtension(uploadFile, "(rdf|ttl|nt|jsonld)")) {
            beatUser(cmd);
        }

        System.out.println("Here will be soon the upload.");
        return false;
    }


    /**
     *
     * @param config the dwerft configuration
     */
	private AbstractXMLtoRDFconverter prpToRdf(DwerftConfig config, String mapping) throws InvalidKeyException {
		if (isEmptyKeys(config)) {
            throw new InvalidKeyException("No PreProducer credentials found.");
        }

        InputStream prpMapping = new AbstractSource().get(
                new File(config.getMappingFolder(), "preproducer.mappings").getAbsolutePath());
        if (prpMapping == null) {
            beatUser("Preproducer mapping not found in " + config.getMappingFolder());
        }


        PreproducerSource pps = new PreproducerSource(config.getPreProducerKey(),
                config.getPreProducerSecret(), config.getPreProducerAppSecret());
		PreProducerToRdf pprdf = new PreProducerToRdf(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				mapping);

		// the order is important, you have to create all classes before you can use them.
		for (String method : pprdf.getAPIMethodOrder()) {
			pprdf.convert(pps.get(method));
		}
	    return pprdf;
    }

    /**
     * Checks weather all credentials are set in the config file or not.
     *
     * @param config - dwerft configuration file
     * @return true iff all credentials are set
     */
    private static boolean isEmptyKeys(DwerftConfig config) {
        return StringUtils.isEmpty(config.getPreProducerAppSecret()) || StringUtils.isEmpty(config.getPreProducerKey())
                || StringUtils.isEmpty(config.getPreProducerSecret());
    }
	
	/**
	 * Dq to rdf.
	 *
	 */
	private AbstractXMLtoRDFconverter dqToRdf(String input, String mapping) {
		InputStream inputStream = new DramaQueenSource().get(input);
        InputStream dqMapping = new AbstractSource().get(
                new File(config.getMappingFolder(), "dramaqueen.mappings").getAbsolutePath());
        if (dqMapping == null) {
            beatUser("Dramaqueen mapping not found in " + config.getMappingFolder());
        }

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				mapping);
		
		dqrdf.convert(inputStream);
	    return dqrdf;
    }
	
	private AbstractXMLtoRDFconverter genericXmlToRdf(String input, String mapping) {
        InputStream inputStream = new AbstractSource().get(input);

        AbstractXMLtoRDFconverter abstractRdf = new AbstractXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				mapping) {

					@Override
					public void processingBeforeConvert() {	}

					@Override
					public void processingAfterConvert() {	}
		};
		
		abstractRdf.convert(inputStream);
	    return abstractRdf;
    }

    private File convertCsvToXml(String input) {
        File f = null;
        try {
            if (hasExtension(input, "csv")) {
                f = new CsvToXmlConverter().convertToXml(input, ';');
            } else {
                f = new AleToXmlConverter().convertToXml(input, '\t');
            }
        } catch (IOException | ParserConfigurationException | TransformerConfigurationException e) {
            L.error("Csv or Ale conversion failed. " + e.getMessage());
        }
        return f;
    }


    // check if we have use a mapping we provide or if the mapping is from outside the classpath
    private String determineMappingFile(String file, File internalFolder) {
        File[] mappings = internalFolder.listFiles();

        for (int i = 0; i < mappings.length; i++) {
            if (StringUtils.equalsIgnoreCase(file, mappings[i].getName())) {
                return mappings[i].getAbsolutePath(); // inside classpath
            }
        }

        return file;
    }

    // the user failed
    private void beatUser(JCommander cmd) {
        L.error("You failed to give valid command line.");
        cmd.usage();
        System.exit(1);
    }

    // again
    private void beatUser(String message) {
        L.error(message);
        System.exit(1);
    }

    // get the Lang object for a specified format
    private Lang getFormat(String format) {
        Lang resultFormat = RDFLanguages.nameToLang(format.toUpperCase());
        // no language found for the specified format
        if (resultFormat == null) {
            resultFormat = Lang.TTL;
        }
        return resultFormat;
    }

    // test if a file has a certain extension
    private boolean hasExtension(String file, String extensions) {
        return StringUtils.substringAfterLast(file, ".").toLowerCase().matches(extensions);
    }
}
