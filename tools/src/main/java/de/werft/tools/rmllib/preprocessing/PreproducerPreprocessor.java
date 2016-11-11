package de.werft.tools.rmllib.preprocessing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.atteo.xmlcombiner.XmlCombiner;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.werft.tools.general.Document;

/**
 * This preprocessor fetches the xml files from the
 * preproducer api and stores them as a combined xml file
 * in the temp folder.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PreproducerPreprocessor extends BasicPreprocessor {

    private static final String BASE_URL = "https://software.preproducer.com/api";

    private static final List<String> methodOrder = Arrays.asList(
        "info", "listCharacters", "listCrew", "listDecorations", "listExtras", "listFigures",
        "listScenes", "listSchedule");

    
    /* mappings for item catergories */
    private final static HashMap<String, String> itemMapping = new HashMap<String, String>() {{
        put("itmset", "Set Dressing");
        put("itmprp", "Props");
        put("itmmkp", "Makeup");
        put("itmcst", "Costumes");
        put("itmcam", "Camera/Lighting");
        put("itmsnd", "Sound");
        put("itmcar", "Vehicles");
        put("itmani", "Animals");
        put("itmfxs", "FX/Stunts");
        put("itmprd", "Production");
    }};

    private String key;

    private String secret;

    private String appSecret;
    
    private Map<String,String> idMapping;

    /**
     * Instantiates a new Preproducer preprocessor.
     *  @param key       the key by preproducer
     * @param secret    the secret provided by preproducer
     * @param appSecret the app secret provided by preproducer
     * @param projectUri
     */
    public PreproducerPreprocessor(String key, String secret, String appSecret, String projectUri) {
        super(projectUri);
        this.key = key;
        this.secret = secret;
        this.appSecret = appSecret;
    }

    @Override
    protected URL preprocessInput(Document doc) {
        XmlCombiner combiner;
        Path tmpFile;

        try {
            combiner = new XmlCombiner();

            /* fetch all xml files and combine them */
            for (String method : methodOrder) {
                String parameters = getBasicParameters(method);
                String signature = generateSignature(parameters);
                if (signature != null) {
                    URL url = new URL(BASE_URL + "?" + parameters + "&signature=" + signature);
                    String cleanedContent = removePrefixes(readContent(url));
                    combiner.combine(new ByteArrayInputStream(cleanedContent.getBytes()));
                }
            }

            /* build xml and do pre processing */
            org.w3c.dom.Document result = combiner.buildDocument();
            generateUuidsForNonIdNodes(result);
            
            idMapping = new HashMap<String,String>();
            buildFigureCharacterMapping(result);
            replaceDramaQueenAndPreproducerIds(result);
            replaceReferenceIds(result);
            addProductionId(result);
            correctFormattedScript(result);
            replaceItemCategories(result);
            removeEmptyShootingDays(result);

            /* write to disk */
            tmpFile = Files.createTempFile("prepro", ".xml");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(result);
            StreamResult streamer = new StreamResult(Files.newOutputStream(tmpFile, StandardOpenOption.TRUNCATE_EXISTING));
            transformer.transform(source, streamer);

            return tmpFile.toUri().toURL();
            
        } catch (ParserConfigurationException | IOException | TransformerException | SAXException e) {
            logger.error("Could not fetch and preprocess Preproducer xml.");
        }

        return null;
    }

    /* add the childs as string to the formattedscript to keep the special xml elements */
    private void correctFormattedScript(org.w3c.dom.Document doc) {
        NodeList items = doc.getElementsByTagName("formattedscript");

        for (int i = 0; i < items.getLength(); i++) {
            Node n = items.item(i);
            if (n.hasChildNodes()) {
               n.setTextContent(childsToString(n));
            }
        }

    }

    /* transform a xml child into a string */
    private String childsToString(Node root) {
        NodeList childs = root.getChildNodes();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            builder.append("<").append(child.getNodeName());

            if (child.hasAttributes()) {
                NamedNodeMap attr = child.getAttributes();
                for (int j = 0; j < attr.getLength(); j++) {
                    builder.append(" ").append(attr.item(j).getNodeName()).append("=\"").append(attr.item(j).getTextContent()).append("\"");
                }
            }
            builder.append(">").append(child.getTextContent());

            builder.append("</").append(child.getNodeName()).append(">");
        }
        return builder.toString();
    }
    
    private void addProductionId(org.w3c.dom.Document document) {
    	Node item = document.getElementsByTagName("project").item(0);
    	String[] split = getProjectUri().split("/");
    	((Element)item).setAttribute("productionId", split[split.length-1]);
    }

    /* set the dqid as id and the old id as ppid */
    private void replaceDramaQueenAndPreproducerIds(Node root) {
        if (root != null && root.hasChildNodes()) {
            NodeList childs = root.getChildNodes();

            /* traverse the xml tree */
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                /* if we have both dramaqueen and preproducer id, add a new preproducer id attribute and replace normal id with dramaqueen */
                if (child.hasAttributes() && child.getAttributes().getNamedItem("id") != null && child.getAttributes().getNamedItem("dramaqueenid") != null) {
                	String ppid = ((Element) child).getAttribute("id");
                	String dqid = ((Element) child).getAttribute("dramaqueenid");
                    ((Element) child).setAttribute("ppid", ppid);
                    ((Element) child).setAttribute("id", dqid);
                    idMapping.put(ppid, dqid);
                } else if (child.hasAttributes() && child.getAttributes().getNamedItem("id") != null) {
                	String ppid = ((Element) child).getAttribute("id");
                	((Element) child).setAttribute("ppid", ppid);
                }
                replaceDramaQueenAndPreproducerIds(child);
            }
        }
    }
    
    private void replaceReferenceIds(Node root) {
        if (root != null && root.hasChildNodes()) {
            NodeList childs = root.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                
                if (child.hasAttributes()) {
                	NamedNodeMap attributes = child.getAttributes();
                	for (int j = 0; j < attributes.getLength(); j++) {
                		String name = attributes.item(j).getNodeName();
                		if (!"ppid".equals(name)) {
	                		String value = ((Element) child).getAttribute(name);
	                		if (idMapping.keySet().contains(value)) {
	                			((Element) child).setAttribute(name, idMapping.get(value));
	                		}
                		}
					}
                }
                String value = child.getTextContent();
        		if (idMapping.keySet().contains(value)) {
        			child.setTextContent(idMapping.get(value));
        		}
                replaceReferenceIds(child);
            }
        }
    }
    
    private void buildFigureCharacterMapping(org.w3c.dom.Document root) {
    	
    	Map<String,Node> figurMap = new HashMap<String, Node>();
    	Map<String,Node> charMap = new HashMap<String, Node>();
    	
    	NodeList figurs = root.getElementsByTagName("figur");
    	for (int i = 0; i < figurs.getLength(); i++) {
    		Node item = figurs.item(i);
    		String id = ((Element)item).getAttribute("id");
    		if (id != null && !"".equals(id)) {
    			figurMap.put(id, item);
    		}
		}

    	NodeList characters = root.getElementsByTagName("character");
    	for (int i = 0; i < characters.getLength(); i++) {
    		Node item = characters.item(i);
    		String id = ((Element)item).getAttribute("id");
    		if (id != null && !"".equals(id)) {
    			charMap.put(id, item);
    		}
		}
    	
    	for (String figureId : figurMap.keySet()) {
    		Node figur = figurMap.get(figureId);
    		String dqid = ((Element)figur).getAttribute("dramaqueenid");
    		if (dqid != null && !"".equals(dqid)) {
    			NodeList rel = ((Element)figur).getElementsByTagName("relatedCharacter");
    			if (rel.getLength() != 0) {
		    		String characterRef = rel.item(0).getTextContent();
		    		Node character = charMap.get(characterRef);
		    		((Element)character).setAttribute("dramaqueenid", dqid);
    			}
    		}
		}
    }
    
    private void replaceItemCategories(org.w3c.dom.Document root) {
    	NodeList codes = root.getElementsByTagName("typeCode");
    	for (int i = 0; i < codes.getLength(); i++) {
    		Node code = codes.item(i);
    		String itm = ((Element)code).getTextContent();
    		if (itm != null && !"".equals(itm) && itemMapping.containsKey(itm)) {
    			String maping = itemMapping.get(itm);
    			code.setTextContent(maping);
    		}
		}
    }
    
    private void removeEmptyShootingDays(org.w3c.dom.Document root) {
    	NodeList days = root.getElementsByTagName("shooting-day");
    	for (int i = 0; i < days.getLength(); i++) {
    		Node day = days.item(i);
    		String attribute = ((Element)day).getAttribute("mode");
    		if ("invisible".equals(attribute)) {
    			Node parentNode = day.getParentNode();
    			parentNode.removeChild(day);
    		}
		}
    }

    /* manipulate elements without ids */
    private void generateUuidsForNonIdNodes(org.w3c.dom.Document doc) {
        NodeList addresses = doc.getElementsByTagName("adress");
        for (int i = 0; i < addresses.getLength(); i++) {
            Element address = (Element) addresses.item(i);
            UUID uuid = UUID.randomUUID();
            address.setAttribute("id", uuid.toString());
        }
        
        NodeList companies = doc.getElementsByTagName("company");
        for (int i = 0; i < companies.getLength(); i++) {
            Element company = (Element) companies.item(i);
            UUID uuid = UUID.randomUUID();
            company.setAttribute("id", uuid.toString());
        }
        
        NodeList emergencies = doc.getElementsByTagName("emergency");
        for (int i = 0; i < emergencies.getLength(); i++) {
            Element emergency = (Element) emergencies.item(i);
            UUID uuid = UUID.randomUUID();
            emergency.setAttribute("id", uuid.toString());
        }
        
        NodeList facilities = doc.getElementsByTagName("facility");
        for (int i = 0; i < facilities.getLength(); i++) {
            Element facility = (Element) facilities.item(i);
            UUID uuid = UUID.randomUUID();
            facility.setAttribute("id", uuid.toString());
        }
    }

    /* read xml file from a url connection */
    private String readContent(URL url) {
        StringBuilder builder = new StringBuilder();
        try {
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            logger.error("Could not fetch content from " + url + ".");
        }

        return builder.toString();
    }

    /**
     * Gets basic commands.
     *
     * @param methodName the method name
     * @return the basic commands
     */
    private String getBasicParameters(String methodName) {
        return "key=" + key + "&method=" + methodName + "&time=" + new Date().getTime();
    }

    /**
     * Generate signature string.
     *
     * @param parameters the commands
     * @return the string
     */
    private String generateSignature(String parameters) {
        String result = "";

        try {
            String hmacSecret = appSecret + secret;

            //HMAC generation
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(hmacSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            //Base64 & URL encoding
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(parameters.getBytes()));
            result = URLEncoder.encode(hash, "UTF-8");

        } catch (Exception e) {
            logger.error("Could not generate Preproducer credentials.");
        }
        return result;
    }

    /* remove namespaces from an xml file, this is maybe unnecessary if rml fixed the utf8 problem,
     * code taken from https://stackoverflow.com/a/15996472 */
    private static String removePrefixes(String input1) {
        String ret = null;
        int strStart = 0;
        boolean finished = false;
        if (input1 != null) {
            //BE CAREFUL : allocate enough size for StringBuffer to avoid expansion
            StringBuffer sb = new StringBuffer(input1.length());
            while (!finished) {

                int start = input1.indexOf('<', strStart);
                int end = input1.indexOf('>', strStart);
                if (start != -1 && end != -1) {
                    // Appending anything before '<', including '<'
                    sb.append(input1, strStart, start + 1);

                    String tag = input1.substring(start + 1, end);
                    if (tag.charAt(0) == '/') {
                        // Appending '/' if it is "</"
                        sb.append('/');
                        tag = tag.substring(1);
                    }

                    int colon = tag.indexOf(':');
                    int space = tag.indexOf(' ');
                    if (colon != -1 && (space == -1 || colon < space)) {
                        tag = tag.substring(colon + 1);
                    }
                    // Appending tag with prefix removed, and ">"
                    sb.append(tag).append('>');
                    strStart = end + 1;
                } else {
                    finished = true;
                }
            }
            //BE CAREFUL : use new String(sb) instead of sb.toString for large Strings
            ret = new String(sb);
        }
        return ret;
    }
}
