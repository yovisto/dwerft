package de.werft.tools.rmllib.postprocessing;

import be.ugent.mmlab.rml.model.dataset.RMLDataset;
import de.werft.tools.general.Document;
import org.apache.jena.rdf.model.Model;

/**
 * This class provides post processing possibilities.
 * Thus you get access to the original file, the mapping
 * and the result model as jena model. To add functionality
 * Override {@link BasicPostprocessor#process(Model, Document)}.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public interface Postprocessor {

    /**
     * Manipulate the result model with the help of the mapping and original
     * source.
     *
     * @param dataset - the mapping result for manipulation
     * @param doc - the {@link Document} from the conversion stage
     * @return a new changed output model or null if something bad happens.
     */
    RMLDataset postprocess(RMLDataset dataset, Document doc);
}
