package de.werft.tools.importer.csv;

import com.opencsv.CSVReader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The csv to xml converter is a generic preprocessing tool.
 * A csv with column names as first row are translated into
 * a xml file. The xml file can then be transferred into rdf using the
 * normal converter tools. The converted file will be stored under the original name
 * with the suffix .xml .
 * <p>
 * Shema:<br/>
 * <code>
 * column1 column2 column3
 * a        b       c
 * d        e       f
 * </code>
 * translates into:
 * <code>
 * <row>
 * <column1>a</column1>
 * <column2>b</column2>
 * <column3>c</column3>
 * </row>
 * <row>
 * and so forth
 * </row>
 * </code>
 * <p>
 * </p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CsvToXmlConverter {

    private final static Logger LOGGER = LogManager.getLogger(CsvToXmlConverter.class);

    private DocumentBuilderFactory domFactory;

    private DocumentBuilder domBuilder;

    private TransformerFactory transformerFactory;

    private Transformer transformer;

    /**
     * Instantiates a new Csv to xml converter.
     *
     * @throws ParserConfigurationException      the parser configuration exception
     * @throws TransformerConfigurationException the transformer configuration exception
     */
    public CsvToXmlConverter() throws ParserConfigurationException, TransformerConfigurationException {
        this.domFactory = DocumentBuilderFactory.newInstance();
        this.domBuilder = domFactory.newDocumentBuilder();
        this.transformerFactory = TransformerFactory.newInstance();
        this.transformer = transformerFactory.newTransformer();
        this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        this.transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    }

    /**
     * Convert a normal csv file into a xml file.
     *
     * @param fileLocation the file location
     * @param sepChar      the sep char
     * @throws IOException the io exception
     */
    public void convertToXml(String fileLocation, Character sepChar) throws IOException {
        try {
            // prepare xml document
            Document doc = domBuilder.newDocument();
            // add filename as root element
            Element root = doc.createElement("csv");
            root.setAttribute("src", new File(fileLocation).getName());
            doc.appendChild(root);

            CSVReader reader = new CSVReader(new FileReader(fileLocation), sepChar);
            // get head line
            String[] header = getHeader(reader);
            if (header == null) {
                throw new IOException("File " + fileLocation + " maybe empty. No first line found");
            }

            // iterate over all and create "xml rows"
            skipToFirstDataLine(reader);
            String[] row = null;
            while((row = reader.readNext()) != null) {
                Element rowElement = createRow(header, row, doc);
                root.appendChild(rowElement);
            }
            reader.close();

            // write xml doc to disk;
            Source s = new DOMSource(doc);
            Result result = new StreamResult(
                    new File(new File(fileLocation).getParent(), getBaseName(fileLocation)+ ".xml"));
            transformer.transform(s, result);

        } catch (FileNotFoundException e) {
            throw new IOException("Could not load file, " + fileLocation + " . " + e.getMessage(), e);
        } catch (TransformerException e) {
            throw new IOException("Could not transform xml results. No xml file written. " + e.getMessage(), e);
        }
    }

    private String getBaseName(String file) {
        File f = new File(file);
        return f.getName().substring(0, f.getName().lastIndexOf('.'));
    }

    /**
     * Create a row element.
     * Override if need some special treatments. see {@link AleToXmlConverter#createRow(String[], String[], Document)}
     *
     * @param header the header
     * @param row    the row
     * @param doc    the doc
     * @return the element
     */
    protected Element createRow(String[] header, String[] row, Document doc) {
        Element rowElement = doc.createElement("row");
        for (int i = 0; i < row.length; i++) {
            Element elem = doc.createElement(header[i]);
            elem.appendChild(doc.createTextNode(row[i]));
            rowElement.appendChild(elem);
        }
        return rowElement;
    }

    /**
     * Get header row.
     *
     * @param reader the reader
     * @return the string [ ]
     * @throws IOException the io exception
     */
    protected String[] getHeader(CSVReader reader) throws IOException {
        return reader.readNext();
    }

    /**
     * Skip to first data line.
     *
     * @param reader the reader
     * @throws IOException the io exception
     */
    protected void skipToFirstDataLine(CSVReader reader) throws IOException { }
}
