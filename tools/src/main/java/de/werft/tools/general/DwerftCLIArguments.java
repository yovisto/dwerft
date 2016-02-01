package de.werft.tools.general;

import com.beust.jcommander.Parameter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * The Class DwerftCLIArguments.
 * Contains all valid command line arguments for use with the main method.
 * Uses the jcommander framework.
 */
public class DwerftCLIArguments {

	/** The input file. */
	@Parameter(names = {"-i", "--input"}, description = "Specify an input file. " +
            "Required for DramaQueen and generic conversion. Use generic conversion and provide a *.csv oder *.ale file" +
            "so we convert it to xml and then to rdf.")
	private String inputFile;

	/** The input type. */
	@Parameter(names = {"-t", "--type"},
            description = "Specify an input type. Available options are PreProducer ('prp'), DramaQueen ('dq'), Generic ('g')", required = true)
	private String inputType;
	
	/** The output file. */
	@Parameter(names = {"-o", "--output"}, description = "Specify an RDF output file")
	private String outputFile;

	/** If the generic converter is used, a custom mapping must be specified. */
	@Parameter (names = {"-m", "--mapping"}, description = "Specifiy a custom mapping file for use with the generic XML to RDF converter")
	private String customMapping;

	/** Flag indicating if RDF should be printed to console */
	@Parameter(names = {"-p", "--print"}, description = "Print output to console")
	private boolean printToCli = false;

	/** The output RDF format. */
	@Parameter(names = {"-f", "--format"}, description = "Specify an RDF output format. Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
	private String outputFormat = "ttl";

	/** Prints the help information */
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

	public String getOutputFormat() {
		return outputFormat;
	}

	public boolean isHelp() {
		return help;
	}

    public Lang getFormat() {
        Lang format = RDFLanguages.nameToLang(outputFormat.toUpperCase());
        // no language found for the specified format
        if (format == null) {
            format = Lang.TTL;
        }
        return format;
    }
}
