package de.werft.tools.tailr;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.Arrays;

/**
 * This enum specifies some api calls to the tailr
 * system. Each call knows it's used parameters and these
 * are expanded with the repo url and additional parameters.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public enum TailrApiCall {

    /**
     * The add api call is used for adding new revisions.
     * Only the repo url and key is needed.
     */
    ADD("key="),
    /**
     * The get api call is used for requesting a specified version.
     * The repo url and key is needed and an optional date for a specific version or
     * no date for the latest version.
     */
    GET("key=", "&datetime="),
    /**
     * The delta api call is used for requesting a specific delta.
     * The repo url and key is needed and an optional date for a specific delta or
     * no date for the latest version.
     */
    DELTA("key=", "&datetime=", "&delta=true"),
    /**
     * the list api call retrieves a list of revisions for a key.
     * Only the repo url and key is needed.
     */
    LIST("key=", "&timemap=true");

    private String[] uriParts;
    
    private Class<? extends HttpRequestBase> request;
    
    private TailrApiCall(String... uriParts) {
        this.uriParts = uriParts;
    }

    /**
     * Gets http request uri for different tailr api calls.
     * The ordering of needed or optional parameters for a request is
     * important. At first the repo url is needed, almost every time as second
     * the key is needed. Then additional parameters are expected.
     * See the enum constants for the correct parameter information.
     *
     * @param params the ordered list of needed and optional parameters
     * @return the http request uri
     */
    public String getHttpRequestUri(String... params) {
        if (params.length < 1) {
            throw new UnsupportedOperationException("At least a repo url is needed.");
        }

        // create a new url and remove repo url from parameters
        StringBuilder builder = new StringBuilder();
        builder.append(params[0]);
        String[] tail = removeHead(params);
        String[] parts = uriParts;

        while(parts.length > 0) {
            builder.append(parts[0]);
            if (parts[0].endsWith("=") && tail.length > 0) {
                builder.append(tail[0]);
                tail = removeHead(tail);
            }

            parts = removeHead(parts);
        }

        return builder.toString();
    }

    // removes the head from an array or returns the empty array
    private String[] removeHead(String[] array) {
        if (array.length <= 0) {
            return array;
        } else {
            return Arrays.copyOfRange(array, 1, array.length);
        }
    }
}
