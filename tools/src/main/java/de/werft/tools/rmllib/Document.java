package de.werft.tools.rmllib;

import java.net.URL;

/**
 * Created by ratzeputz on 09.09.16.
 */
public class Document {

    private URL mappingFile;

    private URL inputFile;

    private URL outputFile;

    public Document(URL mappingFile, URL inputFile, URL outputFile) {
        this.mappingFile = mappingFile;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public URL getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(URL mappingFile) {
        this.mappingFile = mappingFile;
    }

    public URL getInputFile() {
        return inputFile;
    }

    public void setInputFile(URL inputFile) {
        this.inputFile = inputFile;
    }

    public URL getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(URL outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public String toString() {
        return "Document{" +
                "mappingFile=" + mappingFile +
                ", inputFile=" + inputFile +
                ", outputFile=" + outputFile +
                '}';
    }
}
