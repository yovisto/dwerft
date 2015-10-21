package de.werft.tools.general;

import com.beust.jcommander.Parameter;

/**
 * The Class DwerftCLIArguments.
 * Contains all valid command line arguments for use with the main method.
 * Uses the jcommander framework.
 */
public class DwerftCLIArguments {

	/** The input file. */
	@Parameter(names = {"-i", "--input"}, description = "Specify an XML input file", required = true)
	private String inputFile;
	
	@Parameter(names = {"-t", "--type"}, description = "Specify an input type. Available options are 'prp', 'dq', and 'g'", required = true)
	private String inputType;
	
	/** The output file. */
	@Parameter(names = {"-o", "--output"}, description = "Specify an RDF output file", required = true)
	private String outputFile;
	
	@Parameter (names = {"-m", "--mapping"}, description = "Specifiy a custom mapping file for use with the generic XML to RDF converter")
	private String customMapping;
	
	/** The print to cli. */
	@Parameter(names = {"-p", "--print"}, description = "Print output to console")
	private boolean printToCli = false;

	/** The help. */
	@Parameter(names = {"-h", "--help"}, help = true)
	private boolean help = false;

	public String getInputFile() {
		return inputFile;
	}

	public String getInputType() {
		return inputType;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getCustomMapping() {
		return customMapping;
	}

	public boolean isPrintToCli() {
		return printToCli;
	}

	public boolean isHelp() {
		return help;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setCustomMapping(String customMapping) {
		this.customMapping = customMapping;
	}

	public void setPrintToCli(boolean printToCli) {
		this.printToCli = printToCli;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}
}
