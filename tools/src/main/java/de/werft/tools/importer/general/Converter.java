package de.werft.tools.importer.general;

import java.io.File;
import java.io.IOException;

/**
 * This interfaces hides a relative complex and divers
 * module. Since there are different converters for different
 * purposes, the interface accepts multiple parameters defining
 * the input T and the generated output E.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 *
 * @param <E> the type parameter
 */
public interface Converter<E> {

    /**
     * Convert an input file or whatever.
     *
     * @param input the input
     * @throws IOException the io exception
     */
    void convert(String input) throws IOException;

    /**
     * Gets the conversion result.
     *
     * @return the result
     */
    E getResult();

    /**
     * Sets a pre-converter which pre processes the
     * input. The converter has to return a file denoting
     * the conversion result.
     *
     * @param c the converter
     */
    void setPreConverter(Converter<File> c);

}
