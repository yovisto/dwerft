package de.werft.tools.importer.csv;

import com.opencsv.CSVReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;

/**
 * This class converts .ale files a specialized csv version
 * into a xml file.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class AleToXmlConverter extends CsvToXmlConverter {

    /**
     * Instantiates a new ALE csv to xml converter.
     *
     * @throws ParserConfigurationException      the parser configuration exception
     * @throws TransformerConfigurationException the transformer configuration exception
     */
    public AleToXmlConverter() throws ParserConfigurationException, TransformerConfigurationException {
        super();
    }

    @Override
    protected String[] getHeader(CSVReader reader) throws IOException {
        String[] row;
        while ((row = reader.readNext()) != null) {
            if (row.length > 0 && row[0].equalsIgnoreCase("column")) {
                String[] header = reader.readNext();
                for (int i = 0; i < header.length; i++) {
                    // do many replacements, because other tools are to stupid
                    header[i] = header[i]
                            .replace("#", " number")
                            .replace("(", "open ").replace(")", " close")
                            .replace("2nd", "second")
                            .replace("/", " slash ")
                            .trim()
                            .replace(" ", "__");
                }
                return header;
            }
        }
        return new String[0];
    }

    @Override
    protected void skipToFirstDataLine(CSVReader reader) throws IOException {
        String[] row;
        while ((row = reader.readNext()) != null) {
            if (row.length > 0 && row[0].equalsIgnoreCase("data")) {
                return;
            }
        }
    }


    @Override
    protected Element createRow(String[] header, String[] row, Document doc) {
        Element rowElement = doc.createElement("row");
        for (int i = 0; i < row.length; i++) {
            if (header[i].equalsIgnoreCase("uuid")) {
                rowElement.setAttribute("uuid", row[i]);
            }
            if (header[i].isEmpty()) {
                continue;
            }

            System.out.println("header " + header[i]);
            Element elem = doc.createElement(header[i]);
            elem.appendChild(doc.createTextNode(row[i]));
            rowElement.appendChild(elem);
        }
        return rowElement;
    }
}
