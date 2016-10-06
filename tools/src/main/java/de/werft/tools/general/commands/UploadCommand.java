package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.werft.tools.general.DwerftConfig;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * this class specifies the upload command used for
 * uploading rdf files to a triple store. It provides the
 * update based on the input granularity and the provided rdf file.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Upload RDF Models to SPARQL endpoints.")
public class UploadCommand {

    @Parameter(arity = 1, required = true, description = "Uploads a file to a specified sparql endpoint. Valid formats are" +
            " *.(rdf|ttl|nt|jsonld)")
    private List<String> uploadFile = Collections.emptyList();

    @Parameter(names = {"-g"}, /*converter = GranularityConverter.class,*/ arity = 1,
            description = "Give a granularity for the upload command. Possible options are 0, 1, 2 where" +
            "0 deletes the given model; 1 inserts a given model; 2 creates a diff with the remote endpoint. Default is 1.")
    private String granularity = "1";

    @Parameter(names = {"-graph"}, arity = 1,
            description = "Provide a graph name to store the rdf. Otherwise the default graph is used.")
    private String graphName = "";

    @Parameter(names = {"-key"}, arity = 1, required = true,
            description = "Provide the key name for versioning.")
    private String keyName = "";

    @Parameter(names = {"-tool"}, arity = 1, required = true,
            description = "Provide the tool name for versioning.")
    private String toolName = "";

    public Granularity getGranularity() {
        try {
            String s = StringUtils.substringAfterLast(granularity, "_");
            return Granularity.valueOf(s);
        } catch (IllegalArgumentException e) {
            return Granularity.LEVEL_1;
        }
    }

    public String getGraphName(DwerftConfig conf) {
        return "".equals(graphName) ? conf.getDefaultGraph() : graphName;
    }

    public String getKey() {
        return keyName + "/" + toolName;
    }

    public File getFile() {
        if (StringUtils.substringAfterLast(uploadFile.get(0), ".").toLowerCase().matches("(rdf|ttl|nt|jsonld)")) {
            return new File(uploadFile.get(0));
        } else {
            throw new ParameterException("Only (rdf|ttl|nt|jsonld) are valid file endings.");
        }
    }

    /**
     * The Granularity.
     */
    public enum Granularity {
        /**
         * The Level 0 deletes data.
         */
        LEVEL_0 ,
        /**
         * The Level 1 inserts data.
         */
        LEVEL_1 ,
        /**
         * Level 2 creates a diff.
         */
        LEVEL_2 ;
    }
}
