package de.werft.tools.general.commands;

import com.beust.jcommander.ParameterException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.werft.tools.general.DwerftTools;
import org.apache.commons.lang.StringUtils;
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

import java.io.File;
import java.io.IOException;

/**
 * The upload command provides access to the dwerft upload service which
 * handles the tailr versions and the upload to a sparql endpoint.
 * This it provides many options for configuring the upload service.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Command(name = "upload", description = "Upload RDF Models to a webservice which handles different versions and storing.")
public class Upload extends DwerftTools {

    @Arguments(description = "Uploads a file to a specified sparql endpoint. Valid formats are *.(rdf|ttl|nt|jsonld)")
    @Required
    @Path(mustExist = true)
    private String file = "";

    @Option(name = {"-m", "--method"}, description = "Give an upload method for the upload command. Possible options are 0, 1, 2 where" +
            "0 deletes the given model; 1 inserts a given model; 2 creates a diff with the remote endpoint. Default is 2.")
    private String method = "2";

    @Option(name = {"-g", "--graph"}, description = "Provide a graph name to store the rdf. Otherwise the default graph is used.")
    private String graph = "";

    @Option(name = {"-k", "--key"}, description = "Provide the key name for versioning which should include the original tool name.")
    @Required
    private String key = "";

    @Override
    public void run() {
        super.run();
        logger.debug("Upload the file " + file);

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials(config.getRemoteUser(), config.getRemotePass());
        provider.setCredentials(AuthScope.ANY, cred);

        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        String url = config.getRemoteUrl()
                + "key=" + key
                + "&graph=" + getGraphName()
                + "&level=" + method;

        logger.debug("Put file with the url " + url);
        HttpPut put = new HttpPut(url);
        put.addHeader("Content-Type", "application/octet-stream");
        put.setEntity(new FileEntity(getFile()));

        try {
            HttpResponse resp = client.execute(put);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Response was: " + resp.getStatusLine());
            }
        } catch (IOException e) {
            logger.error("Upload request failed.", e);
        }
    }

    /* determine which graph we use */
    private String getGraphName() {
        return "".equals(graph) ? config.getDefaultGraph() : graph;
    }

    /* check that we have a valid rdf file */
    private File getFile() {
        if (StringUtils.substringAfterLast(file, ".").toLowerCase().matches("(rdf|ttl|nt|jsonld)")) {
            return new File(file);
        } else {
            throw new ParameterException("Only (rdf|ttl|nt|jsonld) are valid file endings.");
        }
    }
}
