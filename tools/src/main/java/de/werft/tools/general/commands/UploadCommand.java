package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.werft.tools.update.Update;

import java.util.List;

/**
 * FIXME file extension handling
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Upload RDF Models to SPARQL endpoints.")
public class UploadCommand {

    @Parameter(arity = 1, required = true, description = "Uploads a file to a specified sparql endpoint. Valid formats are" +
            " *.(rdf|ttl|nt|jsonld)")
    private List<String> uploadFile;

    @Parameter(names = {"-g"}, /*converter = GranularityConverter.class,*/ arity = 1,
            description = "Give a granularity for the upload command. Possible options are 0, 1, 2 where" +
            "0 deletes the given model; 1 inserts a given model; 2 creates a diff with the remote endpoint. Default is 1.")
    private String granularity = "1";

    @Parameter(names = {"-graph"}, arity = 1, required = true,
            description = "Provide a graph name to store the rdf.")
    private String graphName = "";

    /* FIXME nested class
    public static class GranularityConverter implements IStringConverter<Update.Granularity> {

        @Override
        public Update.Granularity convert(String g) {
            try {
                return Update.Granularity.valueOf(g);
            } catch (IllegalArgumentException e) {
                return Update.Granularity.LEVEL_1;
            }
        }
    }*/

    public String getUploadFile() {
        return uploadFile.get(0);
    }

    public Update.Granularity getGranularity() {
        try {
            return Update.Granularity.valueOf(granularity);
        } catch (IllegalArgumentException e) {
            return Update.Granularity.LEVEL_1;
        }
    }

    public String getGraphName() {
        return graphName;
    }
}
