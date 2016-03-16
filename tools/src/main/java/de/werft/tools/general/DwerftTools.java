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
import java.io.*;
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

        boolean result = false;
        if (!uploadFile.isEmpty() && files.isEmpty()) {
            result = upload(cmd);
        } else if (!files.isEmpty() && uploadFile.isEmpty()) {
            result = convert(cmd);
        } else {
            beatUser(cmd);
        }

        if (result) {
            System.exit(0);
        }
        System.exit(1);
    }

    // the user failed to give a valid cmd
    private void beatUser(JCommander cmd) {
        L.error("You failed to give valid command line.");
        cmd.usage();
        System.exit(1);
    }

    private boolean convert(JCommander cmd) throws InvalidKeyException {
        if (files.size() > 3 && files.size() < 1) {
            beatUser(cmd);
        }

        String input = files.get(0);
        if (files.size() == 1 && hasExtension(input, "(rdf|nt|ttl)")) { // preproducer to rdf
            prpToRdf(config, input);
        } else if (files.size() == 1 && hasExtension(input, "(ale|csv)")) { // csv to xml
            File f = convertCsvToXml(input);
            L.info("Converted " + input + " to " + f);
        } else {

            String output = files.get(1);
            if (!hasExtension(output, "(rdf|nt|ttl)")) {
                beatUser(cmd);
            }

            if (hasExtension(input, "dq") && files.size() == 2) { // dramaqueen to rdf
                dqToRdf(input, output);
            } else if (hasExtension(input, "(ale|csv)") && files.size() == 3) { // csv to rdf
                String file = convertCsvToXml(input).getAbsolutePath();
                L.info("Converted " + input + " to " + file);
                genericXmlToRdf(file, output, files.get(2));
            } else if (hasExtension(input, "xml") && files.size() == 3) { // generic conversion
                genericXmlToRdf(input, output, files.get(2));
            } else {
                beatUser(cmd);
            }
        }

        return true;
    }

    private boolean upload(JCommander cmd) {
        if (hasExtension(uploadFile, "(rdf|ttl|nt|jsonld)")) {
            beatUser(cmd);
        }

        System.out.println("Here will be soon the upload.");
        return false;
    }

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
        //Configure log4j2
        BasicConfigurator.configure();

        try {
            DwerftTools tools = new DwerftTools();
            tools.run(args);
        } catch (InvalidKeyException e1) {
            L.error("Wrong keys configured. " + e1.getMessage());
        }
    }

    /**
     *
     * @param config the dwerft configuration
     */
	private void prpToRdf(DwerftConfig config, String output) throws InvalidKeyException {
		if (isEmptyKeys(config)) {
            throw new InvalidKeyException("No PreProducer credentials found.");
        }

        InputStream prpMapping = new AbstractSource().get(
                new File(config.getMappingFolder(), "preproducer.mappings").getAbsolutePath());
        if (prpMapping == null) {
            L.error("Preproducer mapping not found in " + config.getMappingFolder());
            System.exit(1);
        }


        PreproducerSource pps = new PreproducerSource(config.getPreProducerKey(),
                config.getPreProducerSecret(), config.getPreProducerAppSecret());
		PreProducerToRdf pprdf = new PreProducerToRdf(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				prpMapping);

		// the order is important, you have to create all classes before
		// you can use them.
		for (String method : pprdf.getAPIMethodOrder()) {
			pprdf.convert(pps.get(method));
		}

		pprdf.writeRdfToFile(output, getFormat());

		if (printToCli)
			pprdf.writeRdfToConsole(getFormat());
			
		L.info("Preproducer RDF has been written to " + output);
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
	private void dqToRdf(String input, String output) {
		InputStream inputStream = new DramaQueenSource().get(input);
        InputStream dqMapping = new AbstractSource().get(
                new File(config.getMappingFolder(), "dramaqueen.mappings").getAbsolutePath());
        if (dqMapping == null) {
            L.error("Dramaqueen mapping not found in " + config.getMappingFolder());
            System.exit(1);
        }

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				dqMapping);
		
		dqrdf.convert(inputStream);
		dqrdf.writeRdfToFile(output, getFormat());
		
		if (printToCli)
			dqrdf.writeRdfToConsole(getFormat());
		
		L.info("Dramaqueen RDF has been written to " + output);
	}
	
	private void genericXmlToRdf(String input, String output, String customMapping) {
        InputStream inputStream = new AbstractSource().get(input);

        AbstractXMLtoRDFconverter abstractRdf = new AbstractXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				customMapping) {

					@Override
					public void processingBeforeConvert() {	}

					@Override
					public void processingAfterConvert() {	}
		};
		
		abstractRdf.convert(inputStream);
		abstractRdf.writeRdfToFile(output, getFormat());
		
		if (printToCli)
			abstractRdf.writeRdfToConsole(getFormat());
		
		L.info("Generic RDF has been written to " + output + " using " + input + " as input and " + customMapping + " as mapping");
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

    private InputStream loadFile(File filename) throws FileNotFoundException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (in != null) {
            return in;
        } else {
            throw new FileNotFoundException("File not found " + filename.getAbsolutePath());
        }
    }

    private Lang getFormat() {
        Lang resultFormat = RDFLanguages.nameToLang(format.toUpperCase());
        // no language found for the specified format
        if (resultFormat == null) {
            resultFormat = Lang.TTL;
        }
        return resultFormat;
    }

    private boolean hasExtension(String file, String extensions) {
        return StringUtils.substringAfterLast(file, ".").toLowerCase().matches(extensions);
    }
}
