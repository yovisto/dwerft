package de.werft.tools.old;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * This class transforms old .mapping files into the newer rml mapping.
 *
 * Since the complete transformation is a bit hard, we only transform a subset.
 * We assume that comments are introduced with # and that a mapping consists
 * out of the elements described in the old readme version.
 *
 * We only transform classes and data type properties
 * and this is only a rough hack.
 *
 * We transform mappings that have the following structure:
 * - comments (#) can be everywhere
 * - empty lines can only be between a complete block
 * - a block can only contain comments no empty lines
 * - a block have all parts specified from xmlNodePath to targetPropertyType
 * - every line in a block is terminated by \n
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Mapper {

    private Logger logger;

    public Mapper(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }

    public void transform(File input, File output) {
        logger.debug("Reading input file: " + input);

        try {
            List<Mapping> mappings = readInput(input);
            //logger.debug("Red the following mappings:\n" + mappings); /* maybe to much output */
            mappings = orderMappings(mappings);
            transform(mappings);
            logger.debug("Ordered mappings are:##########################\n" + mappings);
            logger.debug("Write mapping to " + output);
            write(mappings, output);
        } catch (IOException e) {
            logger.error("Could not read mapping file: " + e.getMessage());
        }
    }

    private void write(List<Mapping> mappings, File output) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("@prefix rr:  <http://www.w3.org/ns/r2rml#> .\n" +
        "@prefix rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
        "@prefix ql:  <http://semweb.mmlab.be/ns/ql#> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
        "@prefix foo: <http://filmontology.org/ontology/2.0/> .\n" +
        "@prefix v:   <http://www.w3.org/2006/vcard/ns#> .\n");

        for (Mapping m : mappings) {
            builder.append(m.getRmlPart());
        }

        Files.write(output.toPath(), builder.toString().getBytes());
    }

    private List<Mapping> orderMappings(List<Mapping> oldOrder) {
        List<Mapping> newOrder = new ArrayList<>(oldOrder.size());

        /* first add a class */
        for (Mapping clazz : oldOrder) {
            if (clazz.isClassMapping()) {
                newOrder.add(clazz);

                /* then add all properties which starts with the class path */
                for (Mapping property : oldOrder) {
                    if (isChildProperty(property, clazz)) {
                        newOrder.add(property);
                    }
                }
            }
        }

        return newOrder;
    }

    /* simply translate the old mappings into the new ones */
    private void transform(List<Mapping> mappings) {
        String trunc = "";
        for (Mapping m : mappings) {
            if (m.isClassMapping()) {
                trunc = m.getPath();
            } else if (m.isNodeProperty()) {
                m.shortenPath(trunc);
            }

            m.transform(isNextClassOrLast(mappings, m));
        }
    }

    /* return true is the next mapping is a class or this is the last element in a list */
    private boolean isNextClassOrLast(List<Mapping> oldOrder, Mapping m) {
        try {
            System.out.println(oldOrder.lastIndexOf(m));
            System.out.println("last " + oldOrder.get(oldOrder.lastIndexOf(m) + 1).isClassMapping());
            return oldOrder.get(oldOrder.lastIndexOf(m) + 1).isClassMapping();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("default true ");
            return true;
        }
    }

    private boolean isChildProperty(Mapping property, Mapping parent) {
        return !property.isClassMapping() && (property.isAttrProperty() || property.isNodeProperty())
                && StringUtils.startsWith(property.getPath(), parent.getPath()) &&
                property.getTargetClass().equals(parent.getTargetClass());
    }

    /* read the input file into a list of mapping definitions */
    private List<Mapping> readInput(File input) throws IOException {
        List<Mapping> mappings = new ArrayList<>();
        List<String> chunks = getChunks(input);
        //logger.debug("Read the following chunks:\n" + chunks);

        for (String chunk : chunks) {
            if (!chunk.isEmpty()) {
                /* skip blocks of empty lines */
                logger.debug("Create mapping for chunk: \n" + chunk);
                Mapping m = new Mapping().createMapping(chunk);
                logger.debug("result is:\n" + m);
                mappings.add(m);
            }
        }

        return mappings;
    }

    /* get chunks from the input file a chunk is a block of lines which ends with a single \n */
    private List<String> getChunks(File input) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));

        String line;
        StringBuilder builder = new StringBuilder();
        List<String> chunks = new ArrayList<>();

        while((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                /* append a line if its no comment */
                if (!isComment(line)) {
                    builder.append(line).append("\n");
                }
            } else {
                /* we have a chunk if a block is terminated by a single \n */
                chunks.add(builder.toString());
                builder.delete(0, builder.length());
            }
        }

        return chunks;
    }

    private boolean isComment(String line) {
        return StringUtils.startsWith(line, "#");
    }
}
