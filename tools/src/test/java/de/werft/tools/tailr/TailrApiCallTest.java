package de.werft.tools.tailr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * This parametrized test validates the correct request creation
 * done by the class under test. It validates the various possible
 * api calls to the tailr system.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Parameterized.class)
public class TailrApiCallTest {

    private static final String defaultParams = "tailr/example.org? example.org";

    /**
     * Data collection for testing. The first element is the api call, the second
     * additional parameters for the uri and at least the expected result.
     *
     * @return the collection
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {TailrApiCall.ADD, defaultParams + "", "tailr/example.org?key=example.org"},
                {TailrApiCall.ADD, defaultParams + " blah", "tailr/example.org?key=example.org"},
                {TailrApiCall.ADD, defaultParams + " blah blah", "tailr/example.org?key=example.org"},
                {TailrApiCall.LIST, defaultParams + "", "tailr/example.org?key=example.org&timemap=true"},
                {TailrApiCall.LIST, defaultParams + " blah", "tailr/example.org?key=example.org&timemap=true"},
                {TailrApiCall.LIST, defaultParams + " blah blah", "tailr/example.org?key=example.org&timemap=true"},
                {TailrApiCall.GET, defaultParams + "", "tailr/example.org?key=example.org&datetime="},
                {TailrApiCall.GET, defaultParams + " blah", "tailr/example.org?key=example.org&datetime=blah"},
                {TailrApiCall.GET, defaultParams + " blah blah", "tailr/example.org?key=example.org&datetime=blah"},
                {TailrApiCall.DELTA, defaultParams + "", "tailr/example.org?key=example.org&datetime=&delta=true"},
                {TailrApiCall.DELTA, defaultParams + " blah", "tailr/example.org?key=example.org&datetime=blah&delta=true"},
                {TailrApiCall.DELTA, defaultParams + " blah blah", "tailr/example.org?key=example.org&datetime=blah&delta=true"}
        });
    }

    private TailrApiCall call;

    private String[] parameters;

    private String expected;

    /**
     * Instantiates a new Tailr api call test.
     *
     * @param call     the api call
     * @param param    aditional parameters
     * @param expected the expected result
     */
    public TailrApiCallTest(TailrApiCall call, String param, String expected) {
        this.call = call;
        this.parameters = param.split(" ");
        this.expected = expected;
    }

    /**
     * Test uri creation.
     */
    @Test
    public void testUriCreation() {
        String result = call.getHttpRequestUri(parameters);
        assertThat(expected, is(result));
    }

}