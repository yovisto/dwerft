package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.werft.tools.general.DwerftConfig;
import de.werft.tools.tailr.Tailr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static final Logger L = LogManager.getLogger(VersioningCommand.class);

    @Parameter(arity = 1, required = true,
            description = "Provide a key name used in tailr.")
    private List<String> keyName = new ArrayList<>();

    @Parameter(names = {"-list"}, arity = 0,
            description = "Provide a key name used in tailr and a revision date.")
    private boolean list = false;

    @Parameter(names = {"-show"}, arity = 1,
            description = "Provide a revision date from tailr. See version -list")
    private String revision = "";

    public String getTimemap(DwerftConfig conf) {
        Tailr t = new Tailr(conf.getTailrToken(), conf.getTailrRepo());
        try {
            List<String> revisions = t.getListOfRevisions(keyName.get(0));
            StringBuilder builder = new StringBuilder();
            builder.append("<------- Timemap ------->\n");

            for (int i = 0; i < revisions.size(); i++) {
                builder.append("Revision ").append(i).append(": ")
                        .append(revisions.get(i)).append("\n");
            }

            return builder.toString();
        } catch (IOException e) {
            L.error("No Revision recieved. ", e);
        }
        return "<------- Timemap ------->\nSee the error log.";
    }

    public boolean isList() {
        return list;
    }

    public boolean isShow() {
        return !list && !revision.isEmpty();
    }
}
