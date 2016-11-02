package de.werft.tools.rmllib.preprocessing;

/**
 * This preprocessor changes the parent source node and source predicate.
 * This has to be done since the a csv file can have additional configuration
 * predicates which are from another ontology.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvPreprocessor extends BasicPreprocessor {

    private static final String CSVW_RESOURCE = "http://semweb.mmlab.be/ns/rml#source";

    private static final String CSVW_SOURCE = "http://www.w3.org/ns/csvw#url";

    public CsvPreprocessor(String projectUri) {
        super(projectUri);
    }


    @Override
    protected String getSourceProperty() {
        return CSVW_SOURCE;
    }

    @Override
    protected String getSourceParentNode() {
        return CSVW_RESOURCE;
    }
}
