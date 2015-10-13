package de.werft.tools.general;

import java.util.List;

import com.beust.jcommander.Parameter;

public class DwerftCLIArguments {

		
	@Parameter(names = {"-c", "--conversion"}, arity = 2, description = "Type of coversion to be done", required = true)
	private List<String> conversion;
	
	@Parameter(names = {"-i", "--input"}, description = "Specify an input file if applicable")
	private String inputFile;
	
	@Parameter(names = {"-o", "--output"}, description = "Specify an output file if applicable")
	private String outputFile;
	
	@Parameter(names = {"prpcfg", "--preproducerconfig"}, description = "Specify a preproducer config file")
	private String prpConfigFile = "src/main/resources/config.properties";
	
	@Parameter(names = {"-prpm", "--preproducermapping"}, description = "Specify a custom preproducer mapping")
	private String prpMapping = "src/main/resources/preproducer.mappings";
	
	@Parameter(names = {"-dqm", "--dramaqueenmapping"}, description = "Specify a custom dramaqueen mapping")
	private String dqMapping = "src/main/resources/dramaqueen.mappings";
	
	@Parameter(names = "-tmpdir", description = "Specify a custom temp directory")
	private String tmpDir = System.getProperty("java.io.tmpdir");
	
	@Parameter(names = {"-p", "--print"}, description = "Print output to console")
	private boolean printToCli = false;

	public List<String> getConversion() {
		return conversion;
	}

	public String getInputFile() {
		return inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getPrpConfigFile() {
		return prpConfigFile;
	}

	public String getPrpMapping() {
		return prpMapping;
	}

	public String getDqMapping() {
		return dqMapping;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public boolean isPrintToCli() {
		return printToCli;
	}

	public void setConversion(List<String> conversion) {
		this.conversion = conversion;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setPrpConfigFile(String prpConfigFile) {
		this.prpConfigFile = prpConfigFile;
	}

	public void setPrpMapping(String prpMapping) {
		this.prpMapping = prpMapping;
	}

	public void setDqMapping(String dqMapping) {
		this.dqMapping = dqMapping;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public void setPrintToCli(boolean printToCli) {
		this.printToCli = printToCli;
	}
}
