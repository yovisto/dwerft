package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.rmllib.Document;

/**
 * Created by ratzeputz on 09.09.16.
 */
public interface Preprocessor {

    public Document preprocess(Document doc);
}
