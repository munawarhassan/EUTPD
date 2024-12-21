package com.pmi.tpd.core.user.preference.spi;

import com.pmi.tpd.api.exception.ApplicationException;

/**
 * Adding the ability to store and retrieve text from propertyset.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IExtendedPreferences extends com.pmi.tpd.core.user.preference.IPreferences {

    /**
     * <p>
     * getText.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getText(String key);

    /**
     * <p>
     * setText.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.String} object.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void setText(String key, String value) throws ApplicationException;
}
