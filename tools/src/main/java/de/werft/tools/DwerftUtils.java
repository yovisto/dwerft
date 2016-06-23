package de.werft.tools;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class holds some useful static methods.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class DwerftUtils {

    /**
     * Prints RDF to console in the specified format
     *
     * @param m      the model for printing
     * @param format the RDF format
     */
    public static void writeRdfToConsole(Model m, Lang format) {
        RDFDataMgr.write(System.out, m, format);
    }

    /**
     * Print a RDF model to console with {@link Lang#TTL} as format.
     *
     * @param m the model
     */
    public static void writeRdfToConsole(Model m) {
        writeRdfToConsole(m, Lang.TTL);
    }

    /**
     * A helper method for writing the result into a file.
     *
     * @param filename the resulting filename and directory
     * @param m        the model
     * @param format   the output format e.g. TTL
     * @throws IOException if an error occurs while writing the model
     */
    public static void writeRdfToFile(String filename, Model m, Lang format) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        RDFDataMgr.write(out, m, format);
        out.close();
    }

    /**
     * A helper method for writing the result into a file with
     * {@link Lang#TTL} as format.
     *
     * @param filename the filename
     * @param m        the model
     * @throws IOException if the something fails
     */
    public static void writeRdfToFile(String filename, Model m) throws IOException {
        writeRdfToFile(filename, m, Lang.TTL);
    }
}