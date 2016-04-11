package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the convert subcommand used by the
 * dwerft tools. It takes care of converting format string to
 * {@link Lang}.
 *
 * FIXME file extension handling
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

    /* FIXME nested class
    protected class LangConverter implements IStringConverter<Lang> {

        @Override
        public Lang convert(String format) {
            System.out.println(format);
            Lang resultFormat = RDFLanguages.nameToLang(format.toUpperCase());
            // no language found for the specified format
            if (resultFormat == null) {
                resultFormat = Lang.TTL;
            }
            return resultFormat;
        }
    }*/

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
}
