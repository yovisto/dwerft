package de.werft.tools.general;

import com.google.inject.Inject;
import de.werft.tools.general.commands.Convert;
import de.werft.tools.general.commands.Upload;
import de.werft.tools.general.commands.Version;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
public class DwerftTools implements Runnable {

    protected static final Logger logger = LogManager.getLogger(DwerftTools.class);

    protected DwerftConfig config;

    @Inject
    protected Help help;

    @Option(type = OptionType.GLOBAL, name = "-v", description = "Enables more verbose output.")
    protected boolean verbose;

    public DwerftTools() {
        this.config = ConfigFactory.create(DwerftConfig.class);
    }

    @Override
    public void run() {

    }

    /**
     * Build and run the dwerft tools.
     *
     * @param args the cli arguments
     */
    public static void main(String[] args) {
        //Configure log4j2
        BasicConfigurator.configure();
        Cli.CliBuilder<Runnable> dwerft = Cli.<Runnable>builder("dwerft")
                .withDescription("The dwerft tools for processing and versioning of linked data.")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Version.class)
                .withCommand(Upload.class)
                .withCommand(Convert.class);

        Cli<Runnable> dwerftTools = dwerft.build();
        dwerftTools.parse(args).run();
    }
}