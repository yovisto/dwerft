package de.werft;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new UploadService();
    }

    @Test
    public void testUploadFile() throws IOException {
        byte[] inputBytes = null;

        inputBytes = Files.readAllBytes(new File("src/test/resources/test.ttl").toPath());
        System.out.println("bytes read into array. size = " + inputBytes.length);

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(getBaseUri()).path("upload")
                .queryParam("key", "http://example.org");

        Invocation.Builder builder = target.request(MediaType.APPLICATION_OCTET_STREAM);

        Response resp = builder.put(Entity.entity(inputBytes, MediaType.APPLICATION_OCTET_STREAM));
        System.out.println("response = " + resp.getStatus());
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }
}
