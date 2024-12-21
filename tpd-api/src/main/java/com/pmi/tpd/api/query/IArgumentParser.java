package com.pmi.tpd.api.query;

import java.util.List;

/**
 * Interface for Argument Parser that is used for parsing given string argument from RSQL query according to type of the
 * target property.
 */
public interface IArgumentParser {

    /**
     * Parse given string argument as the specified class type.
     *
     * @param <T>
     *            class type
     * @param argument
     *            string argument
     * @param type
     *            class type
     * @return The instance of the given argument in the specified type.
     * @throws ArgumentFormatException
     *             If the given argument is not parseable to the specified type.
     * @throws IllegalArgumentException
     *             If the specified type is not supported.
     */
    <T> T parse(String argument, Class<T> type) throws ArgumentFormatException, IllegalArgumentException;

    /**
     * Create an array of arguments casted to their correct types.
     *
     * @param arguments
     *            List of all arguments in String format.
     * @param type
     *            type class type.
     * @return The list with instances of the given arguments in the specified type.
     * @throws ArgumentFormatException
     *             If the a given argument is not parseable to the specified type.
     * @throws IllegalArgumentException
     *             If the specified type is not supported.
     */
    <T> List<T> parse(List<String> arguments, Class<T> type) throws ArgumentFormatException, IllegalArgumentException;
}
