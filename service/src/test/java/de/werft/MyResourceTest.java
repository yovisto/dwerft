package de.werft;

import de.hpi.rdf.tailrapi.Tailr;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.readAllBytes;

public class MyResourceTest extends JerseyTest {

    private TailrStub stub = new TailrStub();

    @Override
    protected Application configure() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bind(stub).to(Tailr.class);
            }
        };
        return new UploadService(binder);
    }

    @Test
    public void testUploadFile() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }

    @Test
    public void testUploadFileWithPArams() throws IOException {
        Invocation.Builder builder = target("upload")
                .queryParam("key", "http://example.org")
                .queryParam("graph", "http://filmontology.org")
                .queryParam("level", "2")
                .queryParam("lang", "ttl").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }

    @Test
    public void testUploadFileNoContent() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        Response resp = builder.put(Entity.entity(new byte[0], MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.NO_CONTENT_204, resp.getStatus());
    }


    @Test
    public void testUploadFileBadKey() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, resp.getStatus());
    }

    @Test
    public void testUploadFileBadFormat() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org")
                .queryParam("lang", "blah").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, resp.getStatus());
    }

    @Test
    public void testUploadBadFile() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        byte[] in = Files.readAllBytes(new File("src/test/resources/false.ttl").toPath());
        Response resp = builder.put(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE_406, resp.getStatus());
    }

    @Test
    public void testUploadWorkingTailr() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        byte[] in = Files.readAllBytes(new File("src/test/resources/test.ttl").toPath());
        Response resp = builder.put(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }


    @Test
    public void testUploadNotWorkingTailr() throws IOException {
        stub.setError(true);
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        byte[] in = Files.readAllBytes(new File("src/test/resources/test.ttl").toPath());
        Response resp = builder.put(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.NOT_MODIFIED_304, resp.getStatus());
        stub.setError(false);
    }

    private byte[] getStream() throws IOException {
        return readAllBytes(new File("src/test/resources/test.ttl").toPath());
    }
}
