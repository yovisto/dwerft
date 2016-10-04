package de.werft.tools.rmllib.preprocessing;

/**
 * Created by ratzeputz on 09.09.16.
 */
public class CsvPreprocessor extends BasicPreprocessor {

    private static final String CSVW_SOURCE = "http://www.w3.org/ns/csvw#url";

    private static final String CSVW_RESOURCE = "http://semweb.mmlab.be/ns/rml#source";


    @Override
    protected String getProperty() {
        return CSVW_SOURCE;
    }

    @Override
    protected String getParentNode() {
        return CSVW_RESOURCE;
    }
}
