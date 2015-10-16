
package de.werft.tools.sources;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides api access to PreProducer.
 *
 * It implements both sides, the download of raw xml data from the service
 * and the upload of newly generated xml files towards them.
 * To grant yourself api access copy the "config.template" to "config.properties"
 * and adjust it with your secrets.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
public class PreproducerSource implements Source {

	private final static Logger L = LogManager.getLogger(PreproducerSource.class);
	
	private String key = "";
	
	private String secret = "";
	
	private String appSecret = "" ;	
	
	private static final String BASE_URL = "https://software.preproducer.com/api";
	
	public PreproducerSource(File propertyFile) throws FileNotFoundException {
		getFromPropFile(propertyFile);
	}
	
	
	private void getFromPropFile(File propertyFile) throws FileNotFoundException {
		Properties prop = new Properties();
		
		try {
			InputStream is = new FileInputStream(propertyFile);
		
			prop.load(is);
			this.key = prop.getProperty("pp.key");
			this.secret = prop.getProperty("pp.secret");
			this.appSecret = prop.getProperty("pp.appsecret");
            close(is);

		} catch (IOException e) {
			L.error("Property File " + propertyFile.getPath() + " not found or loaded. " + e.getMessage());
			throw new FileNotFoundException(e.getMessage());
		}
	}
	
	
	public Map<String, String> getBasicParameters(String methodName) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		
		result.put("key", key);
		result.put("method", methodName);
		result.put("time", String.valueOf(new Date().getTime()));		
		
		return result;		
	}
	
	public String convertParametersToRequest(Map<String, String> parameters) {
		String result = "";
		
		for (String param : parameters.keySet()) {
			String value = parameters.get(param);
			result = result + param + "=" + value + "&";
		}
		
		if (result.length() > 0) {
			result = result.substring(0, result.length()-1);
		}	
		
		return result;
	}
	
	
	public String generateSignature(Map<String, String> parameters) {
		String result = "";
		
		try {	
			String hmacSecret = appSecret + secret;		
			String restRequest = convertParametersToRequest(parameters);
			
			//HMAC generation
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(hmacSecret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			
			//Base64 & URL encoding
			String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(restRequest.getBytes()));
			result = URLEncoder.encode(hash, "UTF-8");
			
	    } catch (Exception e) {
	    	L.error("Signature generation failed. " + e.getMessage());
	    }
		return result;
	}


	/**
	 *
	 * @param source The different api source names.
	 *               they are: info, listCharacter, listCrew, listDecorations, listExtras,
	 *               listFigures, listScenes, listSchedule
	 * @return the resulting input stream
	 */
	@Override
	public BufferedInputStream get(String source) {
		Map<String, String> parameters = getBasicParameters(source);		
		String signature = generateSignature(parameters);		
		String restRequest = convertParametersToRequest(parameters);

        // if we an build a valid signature then we construct an inputstream
		if (signature != null) {
            String url = BASE_URL + "?" + restRequest + "&signature=" + signature;
            
            System.out.println(url);
            
			try {
				return new BufferedInputStream(new URL(url).openStream());
			} catch (IOException e) {
				L.error("Api call " + source + " from preproducer is not reachable.");
			}
		}

		
		return null;
	}

	/**
	 * Executes the upload of an XML file to preproducer
	 * 
	 * @param fileContent The content of the XML file as string
	 * @return Response of the webserver
	 */
	@Override
	public boolean send(String content) {
		boolean result = false;

		Map<String, String> parameters = getBasicParameters("postScript");
		String sig = generateSignature(parameters);		
		parameters.put("signature", sig);
		
		try {
			// build http header
			URL url = new URL(BASE_URL);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
			
			parameters.put("payload", content);
			String paramRequest = convertParametersToRequest(parameters);

            // send data to api
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));		
			writer.write(paramRequest);
			writer.flush();
			close(writer);
			close(os);

            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                result = true;
            }        
            conn.disconnect();   
			
		} catch (IOException e) {
			L.error("Could not send data to preproducer.");
		}
		
		return result;
	}
	
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}


    /**
     * Safely close streams and report errors
     *
     * @param c a closable
     */
    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                L.error("Stream is not closed properly");
            }
        }
    }
}