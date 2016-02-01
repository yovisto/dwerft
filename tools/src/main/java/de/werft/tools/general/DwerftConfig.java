package de.werft.tools.general;

import org.aeonbits.owner.Config;

import java.io.File;

/**
 * Implements the owner config system.
 * Extend this interface to implement more configuration properties.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Config.Sources({"file:tools/DwerftConfig.properties", "file:DwerftConfig.properties", "classpath:DwerftConfig.properties"})
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

    @Key("remote.url")
    String getRemoteUrl();

    @Key("remote.pass")
    @DefaultValue("")
    String getRemotePass();

    @Key("remote.user")
    @DefaultValue("")
    String getRemoteUser();
}
