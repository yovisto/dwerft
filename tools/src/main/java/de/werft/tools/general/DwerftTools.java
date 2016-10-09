package de.werft.tools.general;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.TailrClient;
import de.werft.tools.general.commands.ConvertCommand;
import de.werft.tools.general.commands.UploadCommand;
import de.werft.tools.general.commands.VersioningCommand;
import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools {

    /** The Logger. */
	private static Logger L = LogManager.getLogger(DwerftTools.class);
    		
    @Parameter(names = {"-help"}, help = true, description = "Shows this help message.")
    private boolean isHelp = false;


    private DwerftConfig config;

    public DwerftTools() {
        this.config = ConfigFactory.create(DwerftConfig.class);
        //OntologyConstants.setOntologyFile(config.getOntologyFile());
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
    private void run(String[] args) {
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

        try {
            if ("convert".equals(cmd.getParsedCommand())) {
                convert(convert);
            } else if ("upload".equals(cmd.getParsedCommand())) {
                upload(upload);
            } else if ("version".equals(cmd.getParsedCommand())) {
                version(version);
            } else {
                beatUser(cmd);
            }
        } catch (URISyntaxException | IOException e) {
            L.error("Failed to use the tailr versioning.", e);
        }
    }

    private void version(VersioningCommand version) throws URISyntaxException, IOException {
        TailrClient t = VersioningCommand.getClient(config);
        Repository repo = new Repository(config.getTailrUser(), config.getTailrRepo());

        if (version.isList()) {
            List<Memento> revisions = t.getMementos(repo, version.getKey());
            L.info(version.prettifyTimemap(revisions));
        } else if (version.isShow()) {
            if (version.isLatest()) {
                Memento m = t.getLatestMemento(repo, version.getKey());
                RDFDataMgr.write(System.out, m.resolve(), Lang.TTL);
            } else {
                Memento m = new Memento(repo, version.getKey(), new DateTime(version.getRev()));
                RDFDataMgr.write(System.out, m.resolve(), Lang.TTL);
            }

        } else if (version.isDelta()) {
            if (version.isLatest()) {
                L.info(t.getLatestDelta(repo, version.getKey()));
            } else {
                Memento m = new Memento(repo, version.getKey(), new DateTime(version.getDelta()));
                L.info(t.getDelta(m));
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

        try {
            cmd.convert(config);
        } catch (InstantiationException e) {
            L.error("Instantiation failed. " + e.getMessage());
        } catch (IOException e) {
            L.error("Failed to convert " + cmd.getInput() + " . " + e.getMessage());
        }

        L.info("File " + cmd.getInput() + " converted to " + cmd.getOutput() + " successfully.");
    }

    private void upload(UploadCommand upload) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials(config.getRemoteUser(), config.getRemotePass());
        provider.setCredentials(AuthScope.ANY, cred);

        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

        String url = config.getRemoteUrl()
                + "key=" + upload.getKey()
                + "&graph=" + upload.getGraphName(config)
                + "&level=" + upload.getGranularity().ordinal();

        HttpPut put = new HttpPut(url);
        put.addHeader("Content-Type", "application/octet-stream");
        put.setEntity(new FileEntity(upload.getFile()));

        try {
            HttpResponse resp = client.execute(put);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                L.error("Response was: " + resp.getStatusLine());
            }
        } catch (IOException e) {
            L.error("Upload request failed.", e);
        }
    }

    // the user failed
    private void beatUser(JCommander cmd) {
        L.error("You failed to give valid command line.");
        cmd.usage();
        System.exit(1);
    }

    // again but with individual message
    private void beatUser(String message) {
        L.error(message);
        System.exit(1);
    }
}
