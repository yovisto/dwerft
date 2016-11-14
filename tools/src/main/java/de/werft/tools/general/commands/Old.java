package de.werft.tools.general.commands;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import de.werft.tools.general.DwerftTools;
import de.werft.tools.old.Mapper;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This command starts the conversion between the old mappings and
 * the rml files.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Command(name = "old", description = "Converts old .mappings files into .rml.ttl files.")
public class Old extends DwerftTools {

    @Arguments(description = "Input and output files.", title = {"<input>", "<output>"})
    private List<String> files = new ArrayList<>(10);

    @Override
    public void run() {
        super.run();
        logger.debug("Files: <from mapping> <to rml> " + files);

        if (files.size() == 2 && StringUtils.endsWith(files.get(0), ".mappings")
                && StringUtils.endsWith(files.get(1), ".rml.ttl")) {
            Mapper mapper = new Mapper(logger);
            mapper.transform(new File(files.get(0)), new File(files.get(1)));
        } else {
            logger.error("Either no correct input or output file defined. " + files.toString());
        }
    }
}
