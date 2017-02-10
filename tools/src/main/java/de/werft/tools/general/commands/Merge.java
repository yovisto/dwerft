package de.werft.tools.general.commands;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.Required;

import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.TailrClient;
import de.werft.tools.general.DwerftTools;

/**
 * Created by ratzeputz on 23.01.17.
 */
@Command(name = "merge", description = "Merges to tailr repositories, creating a merge file")
public class Merge extends DwerftTools {

    @Arguments(description = "Creates a merge file based on the latest version under the supplied tailr key and specified file.")
    @Required
    @Path(mustExist = true)
    private String file = "";

    @Option(name = {"-k", "--key"}, description = "Provide the key name from tailr which should be used as merging basis.")
    @Required
    private String key = "";

    @Option(name = {"-f", "--format"}, description = "Provide a language definition such as ttl, ntm, jsonld")
    private String lang = "ttl";

    @Option(name = {"-p", "--private"}, description = "If the tailr repository is private.")
    private boolean isPrivate = false;

    @Override
    public void run() {
        super.run();
        logger.info("p is " + isPrivate);
        logger.debug("Merging the file " + file);

        try {
            TailrClient client = getClient();
            Repository repo = new Repository(config.getTailrUser(), config.getTailrRepo());

            try {
                Memento m = client.getLatestMemento(repo, key);
                //System.out.println(m.getMementoUri());
                String old = convertMementoToNtTriples(m);
                String input = convertToNtTriples(new FileInputStream(file), RDFLanguages.nameToLang(lang));
                merge(old, input);

            } catch (IOException e) {
                /* key not found */
                logger.info("Ne previous version found. Just upload the file as usual.");
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    /* build a new tailr client */
    private TailrClient getClient() throws URISyntaxException {
        return TailrClient.getInstance(config.getTailrBase(), config.getTailrUser(), config.getTailrToken(), isPrivate);
    }

    /* check if an inputstream is valid rdf */
    private String convertToNtTriples(InputStream stream, Lang format) {
        StringWriter writer = new StringWriter();
        try {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, stream, format);
            RDFDataMgr.write(writer, m, Lang.NT);
        } catch (RiotException e) {
            logger.error("Failed to convert the input to n-triples.", e);
        }
        return writer.toString();
    }

    /* check if an inputstream is valid rdf */
    private String convertMementoToNtTriples(Memento m) {
        StringWriter writer = new StringWriter();
        try {
            Graph g = m.resolve();
            RDFDataMgr.write(writer, g, Lang.NT);
        } catch (RiotException | UnsupportedEncodingException | URISyntaxException e) {
            logger.error("Failed to convert the input to n-triples.", e);
        } catch (IOException e) {
            logger.error("Unable to resolve memento.", e);
        }
        return writer.toString();
    }

    private void merge(String old, String input) {
        Map<String, Set<String>> file1 = new TreeMap<String, Set<String>>();
        Map<String, Set<String>> file2 = new TreeMap<String, Set<String>>();
        
        StringBuilder mergedTriples = new StringBuilder();

        String[] split = old.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file1.get(subpred);
            if (set == null) {
                set = new HashSet<String>();
                file1.put(subpred, set);
            }
            set.add(rest);
        }

        split = input.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file2.get(subpred);
            if (set == null) {
                set = new HashSet<String>();
                file2.put(subpred, set);
            }
            set.add(rest);
        }

        Set<String> keys = new HashSet<String>();
        keys.addAll(file1.keySet());
        keys.addAll(file2.keySet());
        
        for (String key : keys) {
            Set<String> oldTriples = file1.get(key);
            Set<String> newTriples = file2.get(key);

            if (newTriples == null) {
                for (String s : oldTriples) {
                	mergedTriples.append(key+" "+s+"\n");
                }
            } else {
                for (String s : newTriples) {
                	mergedTriples.append(key+" "+s+"\n");
                }
            }

        }
        
        try {
            String name = StringUtils.removeEnd(file, "." + lang);
            name = name + "-merged."+lang;
            
            Model m = ModelFactory.createDefaultModel();
            
            InputStream stream = new ByteArrayInputStream(mergedTriples.toString().getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(m, stream, RDFLanguages.NTRIPLES);
            m.setNsPrefix("for", "http://filmontology.org/resource/");
            m.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");
            RDFDataMgr.write(new FileOutputStream(name), m, RDFLanguages.nameToLang(lang));
            
            logger.info("Merged file written: "+name);
            
        } catch (IOException e) {
            logger.error("Failed to write merged file.", e);
        }
    }

}
