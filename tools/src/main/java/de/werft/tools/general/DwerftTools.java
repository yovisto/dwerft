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


public class DwerftTools {

	/** The Logger. */
	private static final Logger L = Logger.getLogger(DwerftTools.class.getName());
	
	private static String input, output, tmpDir;
	private static boolean printToCLI;
	
	public static void main(String[] args) {
		
		DwerftCLIArguments params = new DwerftCLIArguments();
		JCommander cmd = new JCommander(params);
		
		BasicConfigurator.configure();
		
		try {
			cmd.parse(args);
			
			printToCLI = params.isPrintToCli();
			String conversionFrom = params.getConversion().get(0);
			String conversionTo = params.getConversion().get(1);
			
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
			
			if (conversionFrom.equals("ts")) {
				
				output = params.getOutputFile();
				
				/** triplestore -> preproducer */
				if (conversionTo.equals("prp")) {
					L.info("Exporting data from triple store to Preproducer XML");
					
					PreproducerExporter e = new PreproducerExporter(
							OntologyConstants.SPARQL_ENDPOINT,
							OntologyConstants.ONTOLOGY_FILE,
							output, "9860f0bb-d9a6-45e4-9d03-79e7fefd16fa", "17621");
					e.export();
					
					L.info("Preproducer XML file has been written to " + output);
					
				/** triplestore -> lockit network */	
				} else if (conversionTo.equals("ln")) {
					L.info("Exporting data from triple store to LockitNetwork CSV");
					
					LockitExporter e = new LockitExporter(OntologyConstants.SPARQL_ENDPOINT,
							OntologyConstants.ONTOLOGY_FILE, output, "17621");
					e.export();
					
					L.info("LockitNetwork CSV has been written to " + output);
				} else {
					L.error("Invalid conversion type \"" + params.getConversion().get(0) + " to " + params.getConversion().get(1) + "\"");
				}
				
			} else if (conversionTo.equals("ts")) {
				
				tmpDir = params.getTmpDir();
				
				/** triplestore -> dramaqueen */
				if (conversionFrom.equals("dq")) {
					L.info("Converting dramaqueen XML to RDF");
					dqToRdf(params.getInputFile(), params.getDqMapping());
				
				/** triplestore -> preproducer */
				} else if (conversionFrom.equals("prp")) {
					L.info("Converting preproducer XML to RDF");
					prpToRdf(params.getPrpConfigFile(), params.getPrpMapping());
					
				} else {
					L.error("Invalid conversion type \"" + params.getConversion().get(0) + " to " + params.getConversion().get(1) + "\"");
				}
				
			} else if (conversionFrom.equals("dq") && conversionTo.equals("prp")){
				//TODO
				L.info("DQ -> PRP is still in work.");
			} else {
				L.error("Invalid conversion type \"" + params.getConversion().get(0) + " to " + params.getConversion().get(1) + "\"");
			}
		} catch (ParameterException e) {
			L.error("Could not parse arguments : " + e);
		}
	}
	
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
			
			String prpRdfFile = tmpDir + "/preproducer_rdf.ttl";
			System.out.println(prpRdfFile);
			pprdf.writeRdfToFile(prpRdfFile);
			
			if (printToCLI)
				pprdf.writeRdfToConsole();
			
			L.info("Preproducer RDF has been written to " + prpRdfFile);
			
		} catch (FileNotFoundException e) {
			
			L.error("Could not find config file at " + prpConfig + ". " + e.getMessage());
		}

	}
	
	private static void dqToRdf(String input, String mappingFileName) {
		InputStream inputStream = new DramaQueenSource().get(input);

		DramaqueenToRdf dqrdf = new DramaqueenToRdf(
				OntologyConstants.ONTOLOGY_FILE, 
				OntologyConstants.ONTOLOGY_FORMAT, 
				mappingFileName);
		
		dqrdf.convert(inputStream);
		
		String dqRdfFile = tmpDir + "/dramaqueen_rdf.ttl";
		dqrdf.writeRdfToFile(dqRdfFile);
		
		if (printToCLI)
			dqrdf.writeRdfToConsole();
		
		L.info("Dramaqueen RDF has been written to " + dqRdfFile);
	}
}
