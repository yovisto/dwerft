package de.werft.tools.importer.rmllib.preprocessing;

import de.werft.tools.importer.rmllib.Document;
import org.apache.commons.codec.binary.Base64;
import org.atteo.xmlcombiner.XmlCombiner;
import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ratzeputz on 14.09.16.
 */
public class PreproducerPreprocessor extends BasicPreprocessor {

    private static final String BASE_URL = "https://software.preproducer.com/api";

    private static final List<String> methodOrder = Arrays.asList(
        "info", "listCharacters", "listCrew", "listDecorations", "listExtras", "listFigures",
        "listScenes", "listSchedule");

    private String key;

    private String secret;

    private String appSecret;

    public PreproducerPreprocessor(String key, String secret, String appSecret) {
        this.key = key;
        this.secret = secret;
        this.appSecret = appSecret;
    }

    @Override
    protected URL preprocessInput(Document doc) {
        XmlCombiner combiner = null;
        try {
            combiner = new XmlCombiner();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String parameters;
        Path tmpFile = null;

        try {
            tmpFile = Files.createTempFile("prepro", ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String method : methodOrder) {
            parameters = getBasicParameters(method);
            String signature = generateSignature(parameters);

            // if we an build a valid signature then we construct an inputstream
            if (signature != null) {
                try {
                    URL url = new URL(BASE_URL + "?" + parameters + "&signature=" + signature);
                    System.out.println(url);

                    String cleanedContent = removePrefixes(readContent(url));
                    //Files.write(tmpFile, cleanedContent.getBytes(), StandardOpenOption.CREATE);

                    //BufferedInputStream stream = new BufferedInputStream(url.openStream());
                    combiner.combine(new ByteArrayInputStream(cleanedContent.getBytes()));

                } catch (IOException /*| SAXException*/ e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            combiner.buildDocument(tmpFile);
            return tmpFile.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

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
            e.printStackTrace();
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
            //String restRequest = convertParametersToRequest(parameters);

            //HMAC generation
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(hmacSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            //Base64 & URL encoding
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(parameters.getBytes()));
            result = URLEncoder.encode(hash, "UTF-8");

        } catch (Exception e) {
            //       L.error("Signature generation failed. " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static String removePrefixes(String input1) {
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
