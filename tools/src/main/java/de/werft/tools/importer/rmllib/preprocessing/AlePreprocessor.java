package de.werft.tools.importer.rmllib.preprocessing;

import de.werft.tools.importer.rmllib.Document;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ratzeputz on 09.09.16.
 */
public class AlePreprocessor extends CsvPreprocessor {

    @Override
    protected URL preprocessInput(Document doc) {
        Path newInput = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(doc.getInputFile().toURI())));
            content = StringUtils.substringAfter(content, "Column\n");

            String header = StringUtils.substringBefore(content, "\n");
            header = StringUtils.replace(header, "\t", ";");

            content = StringUtils.substringAfter(content, "\n");

            if (StringUtils.startsWith(content, "\nData\n")) {
                content = StringUtils.substringAfter(content, "Data\n");
            }
            content = StringUtils.replace(content, "\t", ";");
            content = header + "\n" + content;

            newInput = Files.createTempFile("input", ".ale");
            Files.write(newInput, content.getBytes());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            return newInput.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
