package de.werft;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new UploadApp();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
        String response = target("upload").request().get(String.class);
        assertEquals("Got it!", response);
    }
}
