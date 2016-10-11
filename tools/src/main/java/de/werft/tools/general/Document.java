package de.werft.tools.general;

import java.net.URL;

/**
 * A document models the conversion process and combines
 * the input file with the mapping and the corresponding output.
 * <br/>
 * Some classes like {@link de.werft.tools.rmllib.preprocessing.Preprocessor}
 * may change the variables.
 * <br/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Document {

    private URL mappingFile;

    private URL inputFile;

    private URL outputFile;

    /**
     * Instantiates a new Document. Some values may by null.
     *
     * @param mappingFile the mapping file
     * @param inputFile   the input file
     * @param outputFile  the output file
     */
    public Document(URL mappingFile, URL inputFile, URL outputFile) {
        this.mappingFile = mappingFile;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Gets mapping file.
     *
     * @return the mapping file
     */
    public URL getMappingFile() {
        return mappingFile;
    }

    /**
     * Sets mapping file.
     *
     * @param mappingFile the mapping file
     */
    public void setMappingFile(URL mappingFile) {
        this.mappingFile = mappingFile;
    }

    /**
     * Gets input file.
     *
     * @return the input file
     */
    public URL getInputFile() {
        return inputFile;
    }

    /**
     * Sets input file.
     *
     * @param inputFile the input file
     */
    public void setInputFile(URL inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Gets output file.
     *
     * @return the output file
     */
    public URL getOutputFile() {
        return outputFile;
    }

    /**
     * Sets output file.
     *
     * @param outputFile the output file
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        if (!mappingFile.equals(document.mappingFile)) return false;
        if (inputFile != null ? !inputFile.equals(document.inputFile) : document.inputFile != null) return false;
        return outputFile != null ? outputFile.equals(document.outputFile) : document.outputFile == null;

    }

    @Override
    public int hashCode() {
        int result = mappingFile.hashCode();
        result = 31 * result + (inputFile != null ? inputFile.hashCode() : 0);
        result = 31 * result + (outputFile != null ? outputFile.hashCode() : 0);
        return result;
    }
}
