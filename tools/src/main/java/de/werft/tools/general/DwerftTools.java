package de.werft.tools.general;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.help.Help;
import de.werft.tools.general.commands.*;
import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;


/**
 * The Class DwerftTools.
 * Contains the main method and handles command line arguments.
 */
@com.github.rvesse.airline.annotations.Cli(name = "dwerft",
description = "The dwerft tools for processing and versioning of linked data.",
defaultCommand = Help.class,
commands = {Help.class, Version.class, Upload.class, Convert.class, Old.class, Merge.class})
public class DwerftTools implements Runnable {

    protected static final Logger logger = LogManager.getLogger(DwerftTools.class);

    protected DwerftConfig config;

    @Option(type = OptionType.GLOBAL, name = "-v", description = "Enables more verbose output.")
    protected boolean verbose;

    public DwerftTools() {
        this.config = ConfigFactory.create(DwerftConfig.class);
    }

    /**
     * Assemble the cli and run the dwerft tools.
     *
     * @param args the cli arguments
     */
    public static void main(String[] args) {
        //Configure log4j2
        BasicConfigurator.configure();
        Cli<Runnable> dwerft = new Cli<>(DwerftTools.class);
        dwerft.parse(args).run();
    }

    @Override
    public void run() {
        if (verbose) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration conf = ctx.getConfiguration();
            conf.getRootLogger().setLevel(Level.DEBUG);
            ctx.updateLoggers();
        }
    }
}