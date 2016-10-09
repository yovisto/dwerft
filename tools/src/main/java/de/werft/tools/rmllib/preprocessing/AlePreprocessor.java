package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.rmllib.Document;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A specialized {@link CsvPreprocessor} which converts the
 * default ale separator "\t" to the common ",". Although the
 * header lines are removed as well as the lines between header and
 * data.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class AlePreprocessor extends CsvPreprocessor {

    @Override
    protected URL preprocessInput(Document doc) {
        Path newInput;
        try {
            /* get file content and remove lines before header */
            String content = new String(Files.readAllBytes(Paths.get(doc.getInputFile().toURI())));
            content = StringUtils.substringAfter(content, "Column\n");

            /* get header line */
            String header = StringUtils.substringBefore(content, "\n");
            content = StringUtils.substringAfter(content, "\n");

            /* remove optional lines between header and data */
            if (StringUtils.startsWith(content, "\nData\n")) {
                content = StringUtils.substringAfter(content, "Data\n");
            }

            /* connect header and data parts as well as replace tabs with semicolons */
            content = header + "\n" + content;
            content = StringUtils.replace(content, "\t", ";");

            /* write new input file and return URL */
            newInput = Files.createTempFile("input", ".ale");
            Files.write(newInput, content.getBytes());
            return newInput.toUri().toURL();
        } catch (IOException | URISyntaxException e) {
            logger.error("Could not replace ale tabs with semicolons.");
        }
        return null;
    }
}
