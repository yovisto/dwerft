package de.werft.tools.general;

import org.aeonbits.owner.Config;

import java.io.File;

/**
 * Implements the owner config system.
 * Extend this interface to implement more configuration properties.
 *
 * The configuration is split in two files. The
 * DwerftConfig contains normal configuration options and
 * DwerftKeys contains credentials. The files are merged after loading.
 * The lookup place is either loaded from the classpath or from
 * the file system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"file:DwerftConfig.properties", "file:DwerftKeys.properties",
        "classpath:DwerftConfig.properties", "classpath:DwerftKeys.properties"})
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

    @Key("dwerft.mappings.pp")
    String getPreProducerMappingName();

    @Key("dwerft.mappings.dq")
    String getDramaQueenMappingName();

    @Key("dwerft.mappings.ale")
    String getAleMappingName();

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

    @Key("remote.graph")
    @DefaultValue("")
    String getDefaultGraph();

    @Key("tailr.token")
    @DefaultValue("")
    String getTailrToken();

    @Key("tailr.base")
    @DefaultValue("")
    String getTailrBase();

    @Key("tailr.user")
    @DefaultValue("")
    String getTailrUser();

    @Key("tailr.repo")
    @DefaultValue("")
    String getTailrRepo();
}
