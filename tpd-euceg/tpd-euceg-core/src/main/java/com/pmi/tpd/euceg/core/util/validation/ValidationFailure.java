package com.pmi.tpd.euceg.core.util.validation;

import java.io.Serializable;

/**
 * Definea a single failure during the validation process. Implementing classes may store any extra information to help
 * callers to identify the source and reasons for the failure.
 *
 * @see BeanValidationFailure
 * @since 1.0
 * @author Christophe Friederich
 */
public interface ValidationFailure extends Serializable {

    /**
     * Returns the object that has generated the failure. For example, if a <code>Person</code> must have a name and a
     * <code>ValidationFailure</code> is created when the user attempts to save it, the <code>Person</code> object would
     * be the failure source.
     *
     * @return the failure's source or null in case a source cannot be defined.
     */
    Object getSource();

    /**
     * Returns an user defined error object.
     *
     * @return a {@link java.lang.Object} object.
     */
    Object getError();

    /**
     * Returns a String representation of the error object. This is used in log messages and exceptions.
     *
     * @return a {@link java.lang.String} object.
     */
    String getDescription();

}
