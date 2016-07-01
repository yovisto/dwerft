package de.werft;

import io.swagger.jaxrs.config.BeanConfig;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ratzeputz on 01.07.16.
 */
public class UploadApp extends Application {

    public UploadApp() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("0.5");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080/");
        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("de.werft");
        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(MyResource.class);
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        return classes;
    }
}
