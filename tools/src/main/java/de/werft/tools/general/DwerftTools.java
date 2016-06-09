package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.DwerftUtils;
import de.werft.tools.general.commands.ConvertCommand;
import de.werft.tools.general.commands.UploadCommand;
import de.werft.tools.general.commands.VersioningCommand;
import de.werft.tools.importer.general.Converter;
import de.werft.tools.tailr.Tailr;
import de.werft.tools.update.Uploader;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

    /** The Logger. */
    private static final Logger L = Logger.getLogger(DwerftTools.class.getName());

    @Parameter(names = {"-help"}, help = true, description = "Shows this help message.")
    private boolean isHelp = false;


    private DwerftConfig config;

    public DwerftTools() {
        this.config = ConfigFactory.create(DwerftConfig.class);
        OntologyConstants.setOntologyFile(config.getOntologyFile());
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        //Configure log4j2
        BasicConfigurator.configure();
        DwerftTools tools = new DwerftTools();
        tools.run(args);
        System.exit(0);
    }

    /**
     * Main entry point
     *
     * @param args the program arguments
     */
    public void run(String[] args) {
        //Parse cli arguments
        ConvertCommand convert = new ConvertCommand();
        UploadCommand upload = new UploadCommand();
        VersioningCommand version = new VersioningCommand();
        JCommander cmd = new JCommander(this);
        cmd.addCommand("convert", convert);
        cmd.addCommand("upload", upload);
        cmd.addCommand("version", version);
        cmd.parse(args);

        if (isHelp) {
            cmd.usage();
            System.exit(0);
        }

        if ("convert".equals(cmd.getParsedCommand())) {
            convert(convert);
        } else if ("upload".equals(cmd.getParsedCommand())) {
            upload(upload);
        } else if ("version".equals(cmd.getParsedCommand())) {
            version(version);
        } else {
            beatUser(cmd);
        }

        System.exit(0);
    }

    private void version(VersioningCommand version) {
        Tailr t = version.getTailrConnector(config);
        if (version.isList()) {
            List<String> revisions = t.getListOfRevisions(version.getKey());
            L.info(version.prettifyTimemap(revisions));
        } else if (version.isShow()) {
            if (version.isLatest()) {
                Model m = t.getLatestRevision(version.getKey());
                RDFDataMgr.write(System.out, m, Lang.TTL);
            } else {
                Model m = t.getRevision(version.getRev(), version.getKey());
                RDFDataMgr.write(System.out, m, Lang.TTL);
            }

        } else if (version.isDelta()) {
            if (version.isLatest()) {
                L.info(t.getLatestDelta(version.getKey()));
            } else {
                L.info(t.getDelta(version.getDelta(), version.getKey()));
            }

        } else {
            beatUser("No versioning option specified or recognized");
        }
    }

    // this method does the actual conversion
    private void convert(ConvertCommand cmd) {
        if (cmd.hasIncorrectFilesCount() || !cmd.isCorrectFileOrder()) {
            beatUser("Invalid amount or order of files given.");
        }
        Converter c;

        try {
            c = cmd.getConverter(config);
            c.convert(cmd.getInput());
            Object result = c.getResult();

            if (result instanceof Model && cmd.isPrintToCli()) {
                DwerftUtils.writeRdfToConsole((Model) result, cmd.getFormat());
            } else if (result instanceof Model) {
                DwerftUtils.writeRdfToFile(cmd.getOutput(), (Model) result, cmd.getFormat());
            }

        } catch (InstantiationException e) {
            L.error("Instantiation failed. " + e.getMessage());
        } catch (IOException e) {
            L.error("Failed to convert " + cmd.getInput() + " . " +e.getMessage());
        }

        L.info("File " + cmd.getInput() + " converted to " + cmd.getOutput() + " successfully.");

    }

    private void upload(UploadCommand upload) {
        HttpAuthenticator auth = new SimpleAuthenticator(config.getRemoteUser(), config.getRemotePass().toCharArray());
        Uploader uploader = new Uploader(config.getRemoteUrl());
        uploader.uploadModel(upload.getUpdate(), upload.getGraphName(), auth);
    }

    // the user failed
    private void beatUser(JCommander cmd) {
        L.error("You failed to give valid command line.");
        cmd.usage();
        System.exit(1);
    }

    // again
    private void beatUser(String message) {
        L.error(message);
        System.exit(1);
    }
}
