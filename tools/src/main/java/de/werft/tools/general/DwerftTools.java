package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.werft.tools.general.commands.ConvertCommand;
import de.werft.tools.general.commands.UploadCommand;
import de.werft.tools.importer.csv.AleToXmlConverter;
import de.werft.tools.importer.csv.CsvToXmlConverter;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdf;
import de.werft.tools.importer.general.AbstractXMLtoRDFconverter;
import de.werft.tools.importer.general.DefaultXMLtoRDFconverter;
import de.werft.tools.importer.preproducer.PreProducerToRdf;
import de.werft.tools.sources.AbstractSource;
import de.werft.tools.sources.DramaQueenSource;
import de.werft.tools.sources.PreproducerSource;
import de.werft.tools.update.Update;
import de.werft.tools.update.UpdateFactory;
import de.werft.tools.update.Uploader;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

    /** The Logger. */
    private static final Logger L = Logger.getLogger(DwerftTools.class.getName());

    @Parameter(names = {"-help"}, help = true, description = "Shows this help message.")
    private boolean isHelp = false;


    private DwerftConfig config;

    public DwerftTools() {
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
        ConvertCommand convert = new ConvertCommand();
        UploadCommand upload = new UploadCommand();
        JCommander cmd = new JCommander(this);
        cmd.addCommand("convert", convert);
        cmd.addCommand("upload", upload);
        cmd.parse(args);

        if (isHelp) {
            cmd.usage();
            System.exit(0);
        }

        if ("convert".equals(cmd.getParsedCommand())) {
            convert(convert);
        } else if ("upload".equals(cmd.getParsedCommand())) {
            upload(upload);
        } else {
            beatUser(cmd);
        }

        System.exit(0);
    }

    // this method does the actual conversion with some guessing magic based on file extensions and file count
    private void convert(ConvertCommand cmd) throws InvalidKeyException {
        if (cmd.hasIncorrectFilesCount() || !cmd.isCorrectFileOrder()) {
            beatUser("Invalid amount or order of files given.");
        }


        AbstractXMLtoRDFconverter converter = null;
        if (cmd.isCsvToXml()) { // csv to xml
            File f = convertCsvToXml(cmd.getInput());
            L.info("Converted " + cmd.getInput() + " to " + f);
        } else if (cmd.isConvertPreproducer()) { // preproducer to rdf
            String mappingFile = determineMappingFile("preproducer.mappings", config.getMappingFolder());
            converter = prpToRdf(config, mappingFile);
        } else if (cmd.isConvertDramaqueen()) { // dramaqueen to rdf
            String mappingFile = determineMappingFile("dramaqueen.mappings", config.getMappingFolder());
            converter = dqToRdf(cmd.getInput(), mappingFile);

        } else if (cmd.isCsvToRdf()) { // csv to rdf
            String file = convertCsvToXml(cmd.getInput()).getAbsolutePath();
            L.info("Converted " + cmd.getInput() + " to " + file);
            String mappingFile = determineMappingFile(cmd.getMapping(), config.getMappingFolder());
            converter = genericXmlToRdf(file, mappingFile);

        } else if (cmd.isConvertGeneric()) { // generic conversion
            String mappingFile = determineMappingFile(cmd.getMapping(), config.getMappingFolder());
            converter = genericXmlToRdf(cmd.getInput(), mappingFile);

        } else {
            beatUser("You failed to give a valid cli");
        }

        // write to cli or fs
        if (converter != null) {
            if (cmd.isPrintToCli()) {
                converter.writeRdfToConsole(cmd.getFormat());
            } else {
                converter.writeRdfToFile(cmd.getOutput(), cmd.getFormat());
            }
            L.info("File " + cmd.getInput() + " converted to " + cmd.getOutput() + " successfully.");
        }
    }

    private boolean upload(UploadCommand upload) {
        Update u = UpdateFactory.createUpdate(upload.getGranularity(), upload.getUploadModel());
        HttpAuthenticator auth = new SimpleAuthenticator(config.getRemoteUser(), config.getRemotePass().toCharArray());
        Uploader uploader = new Uploader(config.getRemoteUrl());
        uploader.uploadModel(u, upload.getGraphName(), auth);
        return true;
    }


    /**
     *
     * @param config the dwerft configuration
     */
	private AbstractXMLtoRDFconverter prpToRdf(DwerftConfig config, String mapping) throws InvalidKeyException {
		if (isEmptyKeys(config)) {
            throw new InvalidKeyException("No PreProducer credentials found.");
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

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				mapping);
		
		dqrdf.convert(inputStream);
	    return dqrdf;
    }
	
	private AbstractXMLtoRDFconverter genericXmlToRdf(String input, String mapping) {
        InputStream inputStream = new AbstractSource().get(input);

        AbstractXMLtoRDFconverter abstractRdf = new DefaultXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				mapping);

		abstractRdf.convert(inputStream);
	    return abstractRdf;
    }

    private File convertCsvToXml(String input) {
        File f = null;
        try {
            if (StringUtils.substringAfterLast(input, ".").toLowerCase().matches("csv")) {
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
}
