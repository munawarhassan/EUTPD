package com.pmi.tpd.core.user.preference;

import java.util.Date;
import java.util.Optional;

import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.exception.ApplicationException;

/**
 * <p>
 * DefaultPreferences class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultPreferences implements IPreferences {

    /**
     * * /**
     */
    private final IPropertyAccessor backingPS;

    /**
     * <p>
     * Constructor for DefaultPreferences.
     * </p>
     */
    public DefaultPreferences(final IPropertyAccessor IPropertyAccessor) {
        backingPS = IPropertyAccessor;
    }

    @Override
    public Long getId() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(final String key) throws ApplicationException {
        return backingPS.exists(key);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Long> getLong(final String key) {
        return backingPS.getLong(key);
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(final String key, final long value) throws ApplicationException {
        throw new ApplicationException("Trying to set a Default preference this is not allowed");
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getString(final String key) {
        return backingPS.getString(key);
    }

    /** {@inheritDoc} */
    @Override
    public void setString(final String key, final String value) throws ApplicationException {
        throw new ApplicationException("Trying to set a Default preference this is not allowed");
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Date> getDate(final String key) {
        return backingPS.getDate(key);
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(final String key, final Date value) throws ApplicationException {
        throw new ApplicationException("Trying to set a Default preference this is not allowed");

    }

    /** {@inheritDoc} */
    @Override
    public Optional<Boolean> getBoolean(final String key) {
        return backingPS.getBoolean(key);
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(final String key, final boolean b) throws ApplicationException {
        throw new ApplicationException("Trying to set a Default preference this is not allowed");
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final String key) throws ApplicationException {
        throw new ApplicationException("Trying to set a Default preference this is not allowed");
    }

}
