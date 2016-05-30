package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the convert subcommand used by the
 * dwerft tools. It takes care of converting format string to
 * {@link Lang}.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Converts XML,CSV,ALE files to RDF.")
public class ConvertCommand {

    // main commands
    @Parameter(variableArity = true, required=true, description = "Starts conversion process. Based on the file extension we determine" +
            " which converter is used.\n Available inputs are *.dp for dramaqueen; *.ale for ALE; *.csv for csv; *.xml for Generic;" +
            " no input for preproducer.\n Available outputs are no output for csv, ale to xml conversion and *.(rdf|ttl|nt) for everything else.\n" +
            " Provide a mapping only for generic conversion. " +
            " Usage: [<input>] <output> [<mapping>]")
    private List<String> files = new ArrayList<>();

    // optional commands
    @Parameter(names = {"-format"}, /*converter = LangConverter.class,*/ arity = 1, description = "Specifies rdf output format. " +
            "Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
    private String format = "ttl";

    @Parameter(names = {"-print"}, description = "Print conversion output to console instead of file.")
    private boolean printToCli = false;

    public List<String> getFiles() {
        return files;
    }

    public Lang getFormat() {
        Lang resultFormat = RDFLanguages.nameToLang(format.toUpperCase());
        // no language found for the specified format
        if (resultFormat == null) {
            resultFormat = Lang.TTL;
        }
        return resultFormat;
    }

    public boolean isPrintToCli() {
        return printToCli;
    }

    public boolean hasIncorrectFilesCount() {
        return files.size() > 3 || files.size() < 1;
    }

    public boolean isCorrectFileOrder() {
        if (files.size() == 1) {
            return hasExtension(files.get(0), "(ale|csv|rdf|nt|ttl)");
        } else if (files.size() == 2 || files.size() == 3) {
            return hasExtension(files.get(0), "(dq|xml|ale|csv)") &&
                    hasExtension(files.get(1), "(rdf|nt|ttl)");
        } else {
            return false;
        }
    }

    public boolean isConvertDramaqueen() {
        return files.size() == 2 && hasExtension(files.get(0), "dq");
    }

    public boolean isCsvToXml() {
        return files.size() == 1 && hasExtension(files.get(0), "(ale|csv)");
    }

    public boolean isConvertPreproducer() {
        return files.size() == 1 && hasExtension(files.get(0), "(rdf|nt|ttl)");
    }

    public boolean isCsvToRdf() {
        return files.size() == 3 && hasExtension(files.get(0), "(ale|csv)");
    }

    public boolean isConvertGeneric() {
        return files.size() == 3 && hasExtension(files.get(0), "xml");
    }

    public String getMapping() {
        if (isCsvToRdf() || isConvertGeneric()) {
            return files.get(2);
        } else {
            return "";
        }
    }

    public String getInput() {
        if (isConvertPreproducer()) {
            return "";
        } else {
            return files.get(0);
        }
    }

    public String getOutput() {
        if (isConvertPreproducer()) {
            return files.get(0);
        } else {
            return files.get(1);
        }
    }

    // test if a file has a certain extension
    private boolean hasExtension(String file, String extensions) {
        return StringUtils.substringAfterLast(file, ".").toLowerCase().matches(extensions);
    }
}
