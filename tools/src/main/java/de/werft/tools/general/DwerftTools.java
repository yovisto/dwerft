package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdf;
import de.werft.tools.importer.general.AbstractXMLtoRDFconverter;
import de.werft.tools.importer.preproducer.PreProducerToRdf;
import de.werft.tools.sources.AbstractSource;
import de.werft.tools.sources.DramaQueenSource;
import de.werft.tools.sources.PreproducerSource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


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

			dqMapping = loadFile("mappings/dramaqueen.mappings");
			prpMapping = loadFile("mappings/preproducer.mappings");
			
			
			//Assign local variables
			String inputType = params.getInputType();
			String invalidInputType = "Invalid input type \"" + inputType + "\"";
			
			//Print help
			if (params.isHelp()) {
				cmd.usage();
			}
			
			//Make sure input and output has been specified
			else if (!(input.isEmpty() && output.isEmpty())) {
				
				/**
				 * (1) dramaqueen to rdf
				 * (2) preproducer to rdf		
				 * (3) generic XML file to rdf. This requires a custom mapping file
				 */
				if (inputType.equals("dq")) {
					dqToRdf();
				} else if (inputType.equals("prp")) {
					prpToRdf();
				} else if (inputType.equals("g") && !params.getCustomMapping().isEmpty()) {
					genericXmlToRdf(params.getCustomMapping());
				} else {
					L.error(invalidInputType);
                    cmd.usage();
				}
			}

		} catch (ParameterException e) {
			L.error("Could not parse arguments : " + e);
            cmd.usage();
		}
	}

	
	/**
	 * Prp to rdf.
	 *
	 * @param prpConfig the prp config
	 */
	private static void prpToRdf(String prpConfig) {
		
		try {
			PreproducerSource pps = new PreproducerSource(new File(prpConfig));
			PreProducerToRdf pprdf = new PreProducerToRdf(
					OntologyConstants.ONTOLOGY_FILE,
					OntologyConstants.ONTOLOGY_FORMAT,
					prpMapping);

	        // the order is important, you have to create all classes before
	        // you can use them.
			pprdf.convert(pps.get("info"));
			pprdf.convert(pps.get("listCharacters"));
			pprdf.convert(pps.get("listCrew"));
			pprdf.convert(pps.get("listDecorations"));
			pprdf.convert(pps.get("listExtras"));
			pprdf.convert(pps.get("listFigures"));
			pprdf.convert(pps.get("listScenes"));
			pprdf.convert(pps.get("listSchedule"));
			
			pprdf.writeRdfToFile(output, outputFormat);
			
			if (printToCLI)
				pprdf.writeRdfToConsole(outputFormat);
			
			L.info("Preproducer RDF has been written to " + output);
			
		} catch (FileNotFoundException e) {
			L.error("Could not find config file at " + prpConfig + ". " + e.getMessage());
		}

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
	
	private static void prpToRdf() {
		
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

    private static InputStream loadFile(String filename) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        return cl.getResourceAsStream(filename);
    }
}
