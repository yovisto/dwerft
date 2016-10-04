
package de.werft.tools.old.sources;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class provides api access to PreProducer.
 * <p>
 * It implements both sides, the download of raw xml data from the service
 * and the upload of newly generated xml files towards them.
 *
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PreproducerSource implements Source {

	private final static Logger L = LogManager.getLogger(PreproducerSource.class);
	
	private String key = "";
	
	private String secret = "";
	
	private String appSecret = "" ;	
	
	private static final String BASE_URL = "https://software.preproducer.com/api";

    /**
     * Constructor for use with the preproducer API
     *
     * @param key       the key
     * @param secret    the secret
     * @param appSecret the app secret
     */
    public PreproducerSource(String key, String secret, String appSecret) {
		this.key = key;
        this.secret = secret;
        this.appSecret = appSecret;
	}


    /**
     * Gets basic commands.
     *
     * @param methodName the method name
     * @return the basic commands
     */
    public Map<String, String> getBasicParameters(String methodName) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		
		result.put("key", key);
		result.put("method", methodName);
		result.put("time", String.valueOf(new Date().getTime()));		
		
		return result;		
	}

    /**
     * Convert commands to request string.
     *
     * @param parameters the commands
     * @return the string
     */
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


    /**
     * Generate signature string.
     *
     * @param parameters the commands
     * @return the string
     */
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
            
			try {
				return new BufferedInputStream(new URL(url).openStream());
			} catch (IOException e) {
				L.error("Api call " + source + " from preproducer is not reachable.");
			}
		}
	
		return null;
	}
}