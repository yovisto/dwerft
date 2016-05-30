package de.werft.tools.tailr;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

/**
 * The Tailr class pushes RDF files
 * to the tailr versioning system and reads
 * actual states and deltas from the tailr
 * system.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Tailr {

    private String token = "";

    private String repoUrl = "";

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
     * @param m the model for revision
     * @return the delta to the last revision
     */
    public boolean addRevision(Model m, String graphUri) {
        try {
            File file = toNtriplesBinary(m);
            String uri = repoUrl + "key=" + graphUri;

            // new client and header informations
            HttpClient client = new DefaultHttpClient();
            HttpPut put = new HttpPut(uri);
            put.setHeader(HeaderConstants.AUTHORIZATION, "token " + token);
            put.setHeader("Content-Type", "application/n-triples");

            // create put request
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new BufferedInputStream(new FileInputStream(file)));
            put.setEntity(entity);
            System.out.println(Arrays.asList(put.getAllHeaders()));

            // get response
            HttpResponse response = client.execute(put);
            System.out.println(response.getStatusLine());

        } catch (IOException e) {
            System.out.println("Could not write binary file for Tailr transmission.\n" + e.getMessage());
        }
        
        return false;
    }

    /**
     * Gets an old revision from a specific date.
     *
     * @param d the revision date
     * @return the revision
     */
    public Model getRevision(Date d) {

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
}
