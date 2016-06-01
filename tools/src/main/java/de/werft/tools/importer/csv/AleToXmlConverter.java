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
    public AleToXmlConverter(Character sepChar) throws ParserConfigurationException, TransformerConfigurationException {
        super(sepChar);
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

        String id = "";
        for (int i = 0; i < row.length; i++) {
        	if (header[i].equalsIgnoreCase("Source__File")) {
        		if (!row[i].trim().isEmpty()) {
        			id = row[i];
        			if (id.contains(".")) {
                    	id = id.substring(0, id.indexOf("."));
        			}
        		}
        	}
        }
        
        if (id.equals("")) {
            for (int i = 0; i < row.length; i++) {
            	if (header[i].equalsIgnoreCase("Name")) {
            		if (!row[i].trim().isEmpty()) {
            			id = row[i];
            			if (id.contains(".")) {
                        	id = id.substring(0, id.indexOf("."));
            			}
            		}
            	}
            }
        }
        
        if (!id.equals("")) {
        	rowElement.setAttribute("uuid", id);
        }

        for (int i = 0; i < row.length; i++) {
            if (header[i].isEmpty() || row[i].trim().isEmpty()) {
                continue;
            }

            Element elem = doc.createElement(header[i]);
            elem.appendChild(doc.createTextNode(row[i]));
            rowElement.appendChild(elem);
        }
        return rowElement;
    }
}
