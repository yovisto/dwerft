package de.werft;

import de.hpi.rdf.tailrapi.Tailr;
import de.hpi.rdf.tailrapi.TailrClient;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.net.URISyntaxException;

/**
 * Created by ratzeputz on 14.07.16.
 */
@ApplicationPath("/")
public class UploadService extends ResourceConfig {

    final static Logger L = LogManager.getLogger(UploadService.class);

    public UploadService() {
        final ServiceConfig conf = initSystem();

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(conf).to(ServiceConfig.class);
                try {
                    bind(TailrClient.getInstance(conf.getTailrBase(), conf.getTailrUser(), conf.getTailrToken(), conf.getTailrPrivateRepo()))
                    .to(Tailr.class);
                    bind(new Uploader(conf.getRemoteUrl())).to(Uploader.class);
                } catch (URISyntaxException e) {
                    L.error("Tailr URI not valid.", e);
                }
            }
        });
    }

    /* custom binders for testing */
    UploadService(AbstractBinder binder) {
        final ServiceConfig conf = initSystem();

        registerSwagger();
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(conf).to(ServiceConfig.class);
            }
        });
        register(binder);
    }

    private ServiceConfig initSystem() {
        packages("de.werft");
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);
        registerSwagger();
        return org.aeonbits.owner.ConfigFactory.create(ServiceConfig.class);
    }

    private void registerSwagger() {
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
