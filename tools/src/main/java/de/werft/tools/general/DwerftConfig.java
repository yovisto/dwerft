package de.werft.tools.general;

import org.aeonbits.owner.Config;

import java.io.File;

/**
 * Implements the owner config system.
 * Extend this interface to implement more configuration properties.
 *
 * Created by Henrik Jürges on 30.10.15.
 */
@Config.Sources({"file:tools/DwerftConfig.properties", "classpath:DwerftConfig.properties"})
public interface DwerftConfig extends Config {

    @Key("pp.key")
    @DefaultValue("")
    String getPreProducerKey();

    @Key("pp.secret")
    @DefaultValue("")
    String getPreProducerSecret();

    @Key("pp.appsecret")
    @DefaultValue("")
    String getPreProducerAppSecret();

    @Key("dwerft.mappings")
    File getMappingFolder();

    @Key("dwerft.ontology")
    File getOntologyFile();
}
