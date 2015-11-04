package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
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

import java.io.*;
import java.security.InvalidKeyException;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

	/** The Logger. */
	private static final Logger L = Logger.getLogger(DwerftTools.class.getName());
	
	/** The tmp dir. */
	private static String input;
    private static String output;
	private static Lang outputFormat;
    private static InputStream dqMapping;
    private static InputStream prpMapping;
	
	/** The print to cli. */
	private static boolean printToCLI;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		//Parse cli arguments
		DwerftCLIArguments params = new DwerftCLIArguments();
		JCommander cmd = new JCommander(params);
		
		//Configure log4j2
		BasicConfigurator.configure();

        DwerftConfig config = ConfigFactory.create(DwerftConfig.class);
        OntologyConstants.setOntologyFile(config.getOntologyFile());


		try {
			cmd.parse(args);
			
			//Assign global variables
			input = params.getInputFile();
			output = params.getOutputFile();
			printToCLI = params.isPrintToCli();
			outputFormat = RDFLanguages.nameToLang(params.getOutputFormat().toUpperCase());

			if (outputFormat == null) {
				outputFormat = Lang.TTL;
			}

            // load predefined mappings
			try {
                dqMapping = loadFile(new java.io.File(config.getMappingFolder(), "dramaqueen.mappings"));
                prpMapping = loadFile(new java.io.File(config.getMappingFolder(), "preproducer.mappings"));
            } catch (FileNotFoundException e) {
                L.error(e.getMessage());
                System.exit(1);
            }

			
			//Assign local variables
			String inputType = params.getInputType();
			
			//Print help
			if (params.isHelp()) {
				cmd.usage();
			}
			
			//Make sure input and output has been specified
			else if (!StringUtils.isEmpty(output)) {
				
				/**
				 * (1) dramaqueen to rdf
				 * (2) preproducer to rdf		
				 * (3) generic XML file to rdf. This requires a custom mapping file
				 */
				if (StringUtils.equals(inputType, "dq")) {
					if (!input.isEmpty()) {
						dqToRdf();
					} else {
						L.error("DramaQueen conversion requires a valid input file");
						cmd.usage();
					}
				} else if (StringUtils.equals(inputType, "prp")) {
                    	prpToRdf(config);
				} else if (StringUtils.equals(inputType, "g")) {
					String customMapping = params.getCustomMapping();
					if (!StringUtils.isEmpty(customMapping) && !StringUtils.isEmpty(input)) {
						genericXmlToRdf(customMapping);
					} else {
						L.error("Using the Generic XML parser requires a valid input file and a custom mapping file.");
						cmd.usage();
					}
				} else {
					L.error("Invalid input type \"" + inputType + "\"");
                    cmd.usage();
				}
			}
        } catch (ParameterException e) {
			L.error("Could not parse arguments : " + e);
            cmd.usage();
        } catch (InvalidKeyException e) {
            L.error("A credentials problem. " + e.getMessage());
        }
    }

    /**
     *
     * @param config the dwerft configuration
     * @throws InvalidKeyException if ne credentials where found
     */
	private static void prpToRdf(DwerftConfig config) throws InvalidKeyException {
		if (isEmptyKeys(config)) {
            throw new InvalidKeyException("No PreProducer credentials found.");
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

		pprdf.writeRdfToFile(output, outputFormat);

		if (printToCLI)
			pprdf.writeRdfToConsole(outputFormat);
			
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
	private static void dqToRdf() {
		InputStream inputStream = new DramaQueenSource().get(input);

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				dqMapping);
		
		dqrdf.convert(inputStream);
		dqrdf.writeRdfToFile(output, outputFormat);
		
		if (printToCLI)
			dqrdf.writeRdfToConsole(outputFormat);
		
		L.info("Dramaqueen RDF has been written to " + output);
	}
	
	private static void genericXmlToRdf(String customMapping) {
		InputStream inputStream = new AbstractSource().get(input);
		
		AbstractXMLtoRDFconverter abstractRdf = new AbstractXMLtoRDFconverter(
				OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT,
				customMapping) {

					@Override
					public void processingBeforeConvert() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void processingAfterConvert() {
						// TODO Auto-generated method stub
						
					}		
		};
		
		abstractRdf.convert(inputStream);
		abstractRdf.writeRdfToFile(output, outputFormat);
		
		if (printToCLI)
			abstractRdf.writeRdfToConsole(outputFormat);
		
		L.info("Generic RDF has been written to " + output + " using " + input + " as input and " + customMapping + " as mapping");
	}

    private static InputStream loadFile(File filename) throws FileNotFoundException {
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
}
