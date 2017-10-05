package de.werft.tools.general.commands;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.werft.tools.general.DwerftTools;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Merge multiple files into one RDF file, adhering some rules.
 *
 * created by Henrik Jürges
 */

@Command(name = "merge", description = "Merges multiple files into a single RDF file.")
public class Merge extends DwerftTools {

    @Arguments(description = "Creates a merged file based on an ontology and the input files. " +
            "The output file shall end with .ttl and the input files shall contain the representation of their ending.", title = "<output file> <input files>")
    @Required
    private List<String> files = new ArrayList<>(5);

    @Override
    public void run() {
        super.run();
        logger.debug("Merge files " + files);
        if (files.size() < 2) {
            logger.error("Need at least two files. One Input and one output.");
        }

        String output = files.get(0);
        files.remove(0);
        Model merge = ModelFactory.createDefaultModel();

        for (String f : files) {
            Model m = RDFDataMgr.loadModel(f);
            merge.add(m);
        }

        try {
            merge.write(new BufferedOutputStream(new FileOutputStream(output)), Lang.TTL.getName());
        } catch (FileNotFoundException e) {
            logger.error("Could not write output file.");
        }
    }
}
