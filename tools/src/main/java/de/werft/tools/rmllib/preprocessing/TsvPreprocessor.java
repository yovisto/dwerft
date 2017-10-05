package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.general.Document;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TsvPreprocessor extends CsvPreprocessor {

    public TsvPreprocessor(String projectUri) {
        super(projectUri);
    }

    @Override
    protected URL preprocessInput(Document doc) {
        logger.info("Converting TSV file into CSV file");
        Path newInput;
        try {
            /* get file content and replace all \t with ; */
            String content = new String(Files.readAllBytes(Paths.get(doc.getInputFile().toURI())));
            content = StringUtils.replace(content, "\t", ";");

            /* write new input file and return URL */
            newInput = Files.createTempFile("input", ".csv");
            Files.write(newInput, content.getBytes());
            return newInput.toUri().toURL();
        } catch (IOException | URISyntaxException e) {
            logger.error("Could not replace ale tabs with semicolons.");
        }
        return null;
    }
}
