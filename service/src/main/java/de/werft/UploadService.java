package de.werft;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by ratzeputz on 14.07.16.
 */
@ApplicationPath("/")
public class UploadService extends ResourceConfig {

    public UploadService() {
        packages("de.werft");
        register(MultiPart.class);
        register(MultiPartFeature.class);
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        // init swagger core
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("0.5");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080/");
        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("de.werft");
        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);
    }
}
