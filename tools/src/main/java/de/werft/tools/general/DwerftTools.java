package de.werft.tools.general;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.werft.tools.exporter.LockitExporter;
import de.werft.tools.exporter.PreproducerExporter;
import de.werft.tools.importer.dramaqueen.DramaqueenToRdf;
import de.werft.tools.importer.preproducer.PreProducerToRdf;
import de.werft.tools.sources.DramaQueenSource;
import de.werft.tools.sources.PreproducerSource;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

	/** The Logger. */
	private static final Logger L = Logger.getLogger(DwerftTools.class.getName());
	
	/** The tmp dir. */
	private static String input, output;
	
	/** The print to cli. */
	private static boolean printToCLI;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		DwerftCLIArguments params = new DwerftCLIArguments();
		JCommander cmd = new JCommander(params);
		
		BasicConfigurator.configure();
		
		try {
			cmd.parse(args);
			
			input = params.getInputFile();
			output = params.getOutputFile();
			printToCLI = params.isPrintToCli();			
			
			String conversionFrom = params.getConversion().get(0);
			String conversionTo = params.getConversion().get(1);
			String invalidConversionError = "Invalid conversion type \"" + conversionFrom + " to " + conversionTo + "\"";
			
			/**
			 * (1) Export: Source is triple store - only output file required
			 * 		- triplestore -> preproducer xml
			 * 		- triple store -> lockit network csv
			 * (2) Destination is triple store - input file is only required for dramaqueen import, output file optional
			 * 		- dramaqueen -> triple store 
			 * 		- preproducer -> triple store
			 * (3) Local source and destination - both input and output file required
			 * 		- dramaqueen -> preproducer
			 * 
			 * IDs are hardcoded because we are only handling sample data at the moment.
			 */
			
			if (params.isHelp()) {
				cmd.usage();
			} else if (conversionFrom.equals("ts")) {
				if (!output.isEmpty()) {
									
					/** triplestore -> preproducer */
					if (conversionTo.equals("prp")) {
						tsToPrp(params.getProjectID());
						
					/** triplestore -> lockit network */	
					} else if (conversionTo.equals("ln")) {
						L.info("Exporting data from triple store to LockitNetwork CSV");
						
						LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT,
								OntologyConstants.ONTOLOGY_FILE, output, "17621");
						e.export();
						
						L.info("LockitNetwork CSV has been written to " + output);
					} else {
						L.error(invalidConversionError);
					}
				} else {
					L.error("Please specify an output file.");
				}		
			} else if (conversionTo.equals("ts")) {
				
				/** triplestore -> dramaqueen */
				if (conversionFrom.equals("dq")) {
										
					//Make sure an input file has been specified
					if (!(input.isEmpty() && output.isEmpty())) {
						L.info("Converting dramaqueen XML to RDF");
						dqToRdf(params.getDqMapping());
					} else {
						L.error("Please specify an input file.");
					}
					
				/** triplestore -> preproducer */
				} else if (conversionFrom.equals("prp")) {
					
					if (!output.isEmpty()) {
						L.info("Converting preproducer XML to RDF");
						prpToRdf(params.getPrpConfigFile(), params.getPrpMapping());
					} else {
						L.error("Please specify an ouput file");
					}
				} else {
					L.error(invalidConversionError);
				}
				
			} else if (conversionFrom.equals("dq") && conversionTo.equals("prp")){
				
				if (!(input.isEmpty() && output.isEmpty())) {
					L.info("Converting dramaqueen xml to preproducer xml via the triple store");
					
					dqToRdf(params.getDqMapping());
					tsToPrp(params.getProjectID());
				} else {
					L.error("Please specify valid file paths");
				}		
			} else {
				L.error(invalidConversionError);
			}
		} catch (ParameterException e) {
			L.error("Could not parse arguments : " + e);
		}
	}
	
	/**
	 * Ts to prp.
	 *
	 * @param projectID the project id
	 */
	private static void tsToPrp(String projectID) {
		L.info("Exporting data from triple store to Preproducer XML");
		
		PreproducerExporter e = new PreproducerExporter(
				OntologyConstants.SPARQL_ENDPOINT,
				OntologyConstants.ONTOLOGY_FILE,
				output, projectID, "17621");
		e.export();
		
		L.info("Preproducer XML file has been written to " + output);
	}
	
	/**
	 * Prp to rdf.
	 *
	 * @param prpConfig the prp config
	 * @param prpMapping the prp mapping
	 */
	private static void prpToRdf(String prpConfig, String prpMapping) {
		
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
			
			pprdf.writeRdfToFile(output);
			
			if (printToCLI)
				pprdf.writeRdfToConsole();
			
			L.info("Preproducer RDF has been written to " + output);
			
		} catch (FileNotFoundException e) {
			
			L.error("Could not find config file at " + prpConfig + ". " + e.getMessage());
		}

	}
	
	/**
	 * Dq to rdf.
	 *
	 * @param mappingFileName the mapping file name
	 */
	private static void dqToRdf(String mappingFileName) {
		InputStream inputStream = new DramaQueenSource().get(input);

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				mappingFileName);
		
		dqrdf.convert(inputStream);
		
		dqrdf.writeRdfToFile(output);
		
		if (printToCLI)
			dqrdf.writeRdfToConsole();
		
		L.info("Dramaqueen RDF has been written to " + output);
	}
}
