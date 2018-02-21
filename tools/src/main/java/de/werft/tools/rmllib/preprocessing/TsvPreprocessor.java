package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.general.Document;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TsvPreprocessor extends CsvPreprocessor {

    private int splitCol = -1;

    public TsvPreprocessor(String projectUri) {
        super(projectUri);
    }

    public TsvPreprocessor(String projectUri, int splitCol) {
        super(projectUri);
        this.splitCol = splitCol;
    }

    @Override
    protected URL preprocessInput(Document doc) {
        logger.info("Converting TSV file into CSV file");
        Path newInput;
        try {
            /* get file content and replace all \t with ; */
            String content = new String(Files.readAllBytes(Paths.get(doc.getInputFile().toURI())));

            /* handle sub lists by splitting that column and
            * duplicating the row with individual elements */
            if (splitCol != -1) {
                List<String> result = new ArrayList<>();
                String[] rows = StringUtils.split(content, '\n');

                for (String row : rows) {
                    String[] cols = StringUtils.splitPreserveAllTokens(row, '\t');
                    String[] sublist = StringUtils.splitPreserveAllTokens(cols[splitCol], ',');

                    for (String elem : sublist) {
                        cols[splitCol] = elem;
                        result.add(StringUtils.join(cols, ';'));
                    }
                }
                content = StringUtils.join(result, '\n');
            } else {
                content = StringUtils.replace(content, "\t", ";");
            }

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
