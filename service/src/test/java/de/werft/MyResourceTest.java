package de.werft;

import de.hpi.rdf.tailrapi.Tailr;
import de.werft.update.Uploader;
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

/**
 * Test the rest service using stubs for underlying classes.
 * No modification is done, all tests are idempotent and only
 * validate the HTTP Response codes.
 *
 * Created by Henrik Jürges
 */
public class MyResourceTest extends JerseyTest {

    private TailrStub stub = new TailrStub();

    @Override
    protected Application configure() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bind(stub).to(Tailr.class);
                bind(new UploaderStub("localhost:8080")).to(Uploader.class);
            }
        };
        return new UploadService(binder);
    }

    /**
     * Test a minimal upload file.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadFile() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }

    /**
     * Test upload file with full parameters.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadFileWithParams() throws IOException {
        Invocation.Builder builder = target("upload")
                .queryParam("key", "http://example.org")
                .queryParam("graph", "http://filmontology.org")
                .queryParam("level", "2")
                .queryParam("lang", "ttl").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }

    /**
     * Test upload file with no content.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadFileNoContent() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        Response resp = builder.put(Entity.entity(new byte[0], MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.NO_CONTENT_204, resp.getStatus());
    }


    /**
     * Test upload file bad tailr key.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadFileBadKey() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, resp.getStatus());
    }

    /**
     * Test upload file with bad format.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadFileBadFormat() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org")
                .queryParam("lang", "blah").request();

        Response resp = builder.put(Entity.entity(getStream(), MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, resp.getStatus());
    }

    /**
     * Test upload not rdf file.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadBadFile() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        byte[] in = Files.readAllBytes(new File("src/test/resources/false.ttl").toPath());
        Response resp = builder.put(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE_406, resp.getStatus());
    }

    /**
     * Test upload with "correctly" working tailr.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testUploadWorkingTailr() throws IOException {
        Invocation.Builder builder = target("upload").queryParam("key", "http://example.org").request();

        byte[] in = Files.readAllBytes(new File("src/test/resources/test.ttl").toPath());
        Response resp = builder.put(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(HttpStatus.OK_200, resp.getStatus());
    }


    /**
     * Test upload without working tailr.
     *
     * @throws IOException the io exception
     */
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
