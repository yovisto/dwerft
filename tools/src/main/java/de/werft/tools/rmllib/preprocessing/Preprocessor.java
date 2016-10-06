package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.rmllib.Document;

/**
 * Interface for apply a preprocess on a document.
 * The main idea is that a preprocessor can either fetch
 * the data and provide access with temporary files or/and
 * manipulate the content before conversion.
 * <br/>
 * The Preprocessor is used by the {@link de.werft.tools.rmllib.RmlMapper}
 * and in order to add new preprocessors to the mapper
 * use as extension point the {@link BasicPreprocessor}
 * which provides some general functionality.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public interface Preprocessor {

    /**
     * The preprocesser takes a document and
     * returns a new document where same values
     * maybe have changed.
     *
     * @param doc - a {@link Document}
     * @return - the new {@link Document}
     */
    Document preprocess(Document doc);
}
