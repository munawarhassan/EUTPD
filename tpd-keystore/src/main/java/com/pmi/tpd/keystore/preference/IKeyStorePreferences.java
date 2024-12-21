package com.pmi.tpd.keystore.preference;

import java.util.Date;
import java.util.Optional;

import com.pmi.tpd.api.exception.ApplicationException;

/**
 * An interface to represent preferences objects.
 *
 * @author Christophe Friederich
 * @since 2.2
 */
public interface IKeyStorePreferences {

    /**
     * @param key
     * @return
     * @throws ApplicationException
     */
    boolean exists(String key) throws ApplicationException;

    /**
     * @return
     */
    String getAlias();

    /**
     * <p>
     * getLong.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a long.
     */
    Optional<Long> getLong(String key);

    /**
     * <p>
     * setLong.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param i
     *            a long.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void setLong(String key, long i) throws ApplicationException;

    /**
     * <p>
     * getString.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Optional<String> getString(String key);

    /**
     * <p>
     * setString.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.String} object.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void setString(String key, String value) throws ApplicationException;

    /**
     * <p>
     * getDate.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link Date} object.
     */
    Optional<Date> getDate(String key);

    /**
     * <p>
     * setDate.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link Date} object.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void setDate(String key, Date value) throws ApplicationException;

    /**
     * <p>
     * getBoolean.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    Optional<Boolean> getBoolean(String key);

    /**
     * <p>
     * setBoolean.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param b
     *            a boolean.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void setBoolean(String key, boolean b) throws ApplicationException;

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param key
     *            a {@link java.lang.String} key of property to remove.
     * @throws com.pmi.tpd.api.exception.ApplicationException
     *             if any.
     */
    void remove(String key) throws ApplicationException;
}