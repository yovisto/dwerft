package de.werft.tools.rmllib.postprocessing;

import de.werft.tools.general.Document;
import org.apache.jena.rdf.model.Model;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Post process dramaqueen documents.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class DramaqueenPostprocessor extends BasicPostprocessor {

    @Override
    protected Model process(Model model, Document doc) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            /* get the root node */
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(doc.getInputFile().getFile());

        } catch (SAXException | IOException e) {
            logger.error("Could not post process dramaqueen xml. " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error("Could not initialize xml parser. " + e.getMessage());
        }


        return model;
    }
}
