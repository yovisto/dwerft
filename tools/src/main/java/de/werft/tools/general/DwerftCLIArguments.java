package de.werft.tools.general;

import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * The Class DwerftCLIArguments.
 * Contains all valid command line arguments for use with the main method.
 * Uses the jcommander framework.
 */
public class DwerftCLIArguments {

		
	/** 
	 * The conversion. Must always be specified.
	 * Due to the nature of the dwerft tool package not all conversions are currently supported.
	 * The following conversions are valid for use with the -c argument
	 * - Generating XML from RDF
	 * 		triple store -> preproducer (-c ts prp -o /path/to/output)
	 * 		triple store -> dramaqueen (-c ts dq -o /path/to/output)
	 * - Generating RDF from XML 
	 * 		dramaqueen -> RDF (-c dq ts -i /path/to/dramaqueenXML -o /path/to/output)
	 * - Generating RDF from preproducer tool. Thi required valid preproducer credentials.
	 * 		preproducer -> RDF (-c prp ts -o /path/to/output)
	 * 
	 */
	@Parameter(names = {"-c", "--conversion"}, arity = 2, description = "Type of coversion to be done. "
			+ "Available options as of now are "
			+ "ts -> prp, ts -> dq, "
			+ "prp -> ts, dq -> ts", required = true)
	private List<String> conversion;
	
	/** The input file. */
	@Parameter(names = {"-i", "--input"}, description = "Specify an input file if applicable")
	private String inputFile;
	
	/** The output file. */
	@Parameter(names = {"-o", "--output"}, description = "Specify an output file if applicable")
	private String outputFile;
	
	/** The project id. */
	@Parameter (names = {"-pid", "--projectid"}, description = "Specify a project ID for fetching data from the triple store")
	private String projectID = "9860f0bb-d9a6-45e4-9d03-79e7fefd16fa";
	
	/** The prp config file. */
	@Parameter(names = {"prpcfg", "--preproducerconfig"}, description = "Specify a preproducer config file")
	private String prpConfigFile = "src/main/resources/config.properties";
	
	/** The prp mapping. */
	@Parameter(names = {"-prpm", "--preproducermapping"}, description = "Specify a custom preproducer mapping")
	private String prpMapping = "src/main/resources/preproducer.mappings";
	
	/** The dq mapping. */
	@Parameter(names = {"-dqm", "--dramaqueenmapping"}, description = "Specify a custom dramaqueen mapping")
	private String dqMapping = "src/main/resources/dramaqueen.mappings";
	
	/** The print to cli. */
	@Parameter(names = {"-p", "--print"}, description = "Print output to console")
	private boolean printToCli = false;

	/** The help. */
	@Parameter(names = {"-h", "--help"}, help = true)
	private boolean help = false;

	/**
	 * Gets the conversion.
	 *
	 * @return the conversion
	 */
	public List<String> getConversion() {
		return conversion;
	}

	/**
	 * Gets the input file.
	 *
	 * @return the input file
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * Gets the output file.
	 *
	 * @return the output file
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectID() {
		return projectID;
	}

	/**
	 * Gets the prp config file.
	 *
	 * @return the prp config file
	 */
	public String getPrpConfigFile() {
		return prpConfigFile;
	}

	/**
	 * Gets the prp mapping.
	 *
	 * @return the prp mapping
	 */
	public String getPrpMapping() {
		return prpMapping;
	}

	/**
	 * Gets the dq mapping.
	 *
	 * @return the dq mapping
	 */
	public String getDqMapping() {
		return dqMapping;
	}

	/**
	 * Checks if is prints the to cli.
	 *
	 * @return true, if is prints the to cli
	 */
	public boolean isPrintToCli() {
		return printToCli;
	}

	/**
	 * Checks if is help.
	 *
	 * @return true, if is help
	 */
	public boolean isHelp() {
		return help;
	}

	/**
	 * Sets the conversion.
	 *
	 * @param conversion the new conversion
	 */
	public void setConversion(List<String> conversion) {
		this.conversion = conversion;
	}

	/**
	 * Sets the input file.
	 *
	 * @param inputFile the new input file
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Sets the output file.
	 *
	 * @param outputFile the new output file
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectID the new project id
	 */
	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}

	/**
	 * Sets the prp config file.
	 *
	 * @param prpConfigFile the new prp config file
	 */
	public void setPrpConfigFile(String prpConfigFile) {
		this.prpConfigFile = prpConfigFile;
	}

	/**
	 * Sets the prp mapping.
	 *
	 * @param prpMapping the new prp mapping
	 */
	public void setPrpMapping(String prpMapping) {
		this.prpMapping = prpMapping;
	}

	/**
	 * Sets the dq mapping.
	 *
	 * @param dqMapping the new dq mapping
	 */
	public void setDqMapping(String dqMapping) {
		this.dqMapping = dqMapping;
	}

	/**
	 * Sets the prints the to cli.
	 *
	 * @param printToCli the new prints the to cli
	 */
	public void setPrintToCli(boolean printToCli) {
		this.printToCli = printToCli;
	}

	/**
	 * Sets the help.
	 *
	 * @param help the new help
	 */
	public void setHelp(boolean help) {
		this.help = help;
	}
}
