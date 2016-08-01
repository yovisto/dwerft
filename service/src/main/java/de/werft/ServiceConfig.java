package de.werft;

import org.aeonbits.owner.Config;

/**
 * Implements the owner config system.
 * Extend this interface to implement more configuration properties.
 *
 * The configuration is split in two files. The
 * ServiceConfig contains normal configuration options and
 * DwerftKeys contains credentials. The files are merged after loading.
 * The lookup place is either loaded from the classpath or from
 * the file system.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"file:ServiceConfig.properties", "file:ServiceKeys.properties",
        "classpath:ServiceConfig.properties", "classpath:ServiceKeys.properties"})
public interface ServiceConfig extends Config {

    @Key("remote.url")
    String getRemoteUrl();

    @Key("remote.pass")
    @DefaultValue("")
    String getRemotePass();

    @Key("remote.user")
    @DefaultValue("")
    String getRemoteUser();

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
