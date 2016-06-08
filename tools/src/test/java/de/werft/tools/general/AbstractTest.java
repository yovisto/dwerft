package de.werft.tools.general;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang.StringUtils;

/**
 * This class is used as a basic template
 * for all the integration tests done for the dwerft code.
 * Since there are more integration then unit tests these are
 * done within the integration tests.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public abstract class AbstractTest {

    /**
     * The general dwerft config; make sure that all necessary parts
     * are included; e.g. keys and urls
     */
    protected DwerftConfig conf = ConfigFactory.create(DwerftConfig.class);

    protected String testFolder = "src/test/resources/files/";

    protected String verificationFolder = "src/test/resources/verification/";

    /**
     * A simple filename suffix replacement operation.
     * The old suffix after a dot is removed and replaced by a given
     * replacement.
     *
     * @param s      the filename
     * @param suffix the replacement suffix
     * @return the new filename
     */
    protected String replaceSuffix(String s, String suffix) {
        return StringUtils.substringBeforeLast(s, ".") + "." + suffix;
    }
}
