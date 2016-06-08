package de.werft.tools.tailr;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Tailr class pushes RDF files
 * to the tailr versioning system and reads
 * actual states and deltas from the tailr
 * system.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Tailr {

    private static final Logger L = LogManager.getLogger(Tailr.class);

    private String token = "";

    private String repoUrl = "";

    private JsonFactory factory = new JsonFactory();

    /**
     * Instantiates a new connector to the Tailr system.
     *
     * @param token   the token
     * @param repoUrl the repo url
     */
    public Tailr(String token, String repoUrl) {
        this.repoUrl = repoUrl;
        this.token = token;
    }

    /**
     * Add a new revision from a model.
     *
     * @param m        the model for revision
     * @param keyName the graph uri
     * @return the delta to the last revision
     */
    public boolean addRevision(Model m, String keyName) {
        try {
            File file = toNtriplesBinary(m);
            String uri = repoUrl + "key=" + keyName;

            // new client and header informations
            HttpClient client = new DefaultHttpClient();
            HttpPut put = new HttpPut(uri);
            put.setHeader(HeaderConstants.AUTHORIZATION, "token " + token);
            put.setHeader("Content-Type", "application/n-triples");

            // create put request
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new BufferedInputStream(new FileInputStream(file)));
            put.setEntity(entity);

            // get response
            HttpResponse response = client.execute(put);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            L.error("Could not write binary file for Tailr transmission.\n", e);
        }

        return false;
    }

    /**
     * Gets the list of revisions stored in tailr for and uri.
     *
     * @param keyName the graph name uri
     * @return the list of revisions
     */
    public List<String> getListOfRevisions(String keyName) throws IOException {
        List<String> timemap = null;

        try {
            String uri = repoUrl + "key=" + keyName + "&timemap=true";

            // new client and header information
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(uri);
            get.setHeader(HeaderConstants.AUTHORIZATION, "token " + token);

            // get response
            HttpResponse response = client.execute(get);
            L.info("Request to tailr responsed with " + response.getStatusLine());
            timemap = convertTimemap(response.getEntity().getContent());
        } catch (IOException e) {
            throw new IOException("Could not read response from Tailr.\n" + e.getMessage());
        }

        return timemap;
    }

    /**
     * Gets an old revision from a specific date.
     *
     * @param date the revision date
     * @return the revision
     */
    public Model getRevision(String date) {

        return null;
    }

    // converts a model into a n triples file
    private File toNtriplesBinary(Model m) throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File binary = new File(tmpDir, String.valueOf(System.currentTimeMillis()));

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(binary));
        RDFDataMgr.write(out, m, Lang.NT);
        out.close();
        return binary;
    }

    // converts a json timemap to a list of dates
    private List<String> convertTimemap(InputStream is) throws IOException {
        JsonParser parser = factory.createParser(is);
        List<String> timemap = new ArrayList<>();

        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an object");
        }

        while(parser.nextToken() != JsonToken.END_OBJECT) {
            if ("datetime".equals(parser.getCurrentName())) {
                parser.nextToken(); // this is the value
                timemap.add(parser.getValueAsString());
            }
        }

        return timemap;
    }
}
