package de.werft.tools.general.commands;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.TailrClient;
import de.werft.tools.general.DwerftConfig;

/**
 * This command takes care of the connection to the tailr
 * versioning system.
 * It enables a user to view the latest revisions and
 * a rollback if something failed with an old version.
 *
 * A new version is automatically stored if something is uploaded
 * to the sparql server.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Access the tailr versioning system.")
public class VersioningCommand {

	private static Logger L = LogManager.getLogger();

    @Parameter(arity = 1, required = true,
            description = "Provide a key name used in tailr.")
    private List<String> keyName = new ArrayList<>();

    @Parameter(names = {"-list"}, arity = 0,
            description = "Provide a key name used in tailr and a revision date.")
    private boolean list = false;

    @Parameter(names = {"-show"}, arity = 1,
            description = "Provide a revision date from tailr. See version -list or use \"latest\" for the latest revision.")
    private String revision = "";

    @Parameter(names = {"-show-delta"}, arity = 1,
            description = "Provide a revision date from tailr. See version -list or use \"latest\" for the latest revision.")
    private String delta = "";

    public static TailrClient getClient(DwerftConfig conf) throws URISyntaxException {
        return TailrClient.getInstance(conf.getTailrBase(), conf.getTailrUser(), conf.getTailrToken());
    }
    
    public String prettifyTimemap(List<Memento> revisions) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n<------- Timemap ------->\n");

        if (revisions.isEmpty()) {
            builder.append("No revision found.\n");
        } else {
            for (int i = 0; i < revisions.size(); i++) {
                builder.append("Revision ").append(i).append(": ")
                        .append(revisions.get(i).getDateTime()).append("\n");
            }
        }
        return builder.toString();
    }
    
    public String getKey() {
        return keyName.get(0);
    }

    public String getRev() {
        return revision;
    }
    
    public boolean isList() {
        return list;
    }

    public boolean isShow() {
        return !list && !revision.isEmpty() && delta.isEmpty();
    }
    
    public boolean isDelta() {
        return !list && !delta.isEmpty() && revision.isEmpty();
    }
    
    public boolean isLatest() {
        return "latest".equals(delta) || "latest".equals(revision);
    }

    public String getDelta() {
        return delta;
    }
}
