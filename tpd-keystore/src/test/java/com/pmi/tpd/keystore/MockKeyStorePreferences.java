package com.pmi.tpd.keystore;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.keystore.preference.IKeyStorePreferences;

public class MockKeyStorePreferences implements IKeyStorePreferences {

    /** */
    private final String alias;

    /** */
    private final Map<String, Object> map;

    public MockKeyStorePreferences(final String alias) {
        this.alias = alias;
        this.map = Maps.newHashMap();
    }

    public MockKeyStorePreferences(final String alias, final Map<String, Object> values) {
        this.alias = alias;
        this.map = Maps.newHashMap(values);
    }

    @Override
    public boolean exists(final String key) throws ApplicationException {
        return map.containsKey(key);
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Optional<Long> getLong(final String key) {
        return Optional.ofNullable((Long) map.get(key));
    }

    @Override
    public void setLong(final String key, final long i) throws ApplicationException {
        map.put(key, i);
    }

    @Override
    public Optional<String> getString(final String key) {
        return Optional.ofNullable((String) map.get(key));
    }

    @Override
    public void setString(final String key, final String value) throws ApplicationException {
        map.put(key, value);
    }

    @Override
    public Optional<Date> getDate(final String key) {
        return Optional.ofNullable((Date) map.get(key));
    }

    @Override
    public void setDate(final String key, final Date value) throws ApplicationException {
        map.put(key, value);
    }

    @Override
    public Optional<Boolean> getBoolean(final String key) {
        return Optional.ofNullable((Boolean) map.get(key));
    }

    @Override
    public void setBoolean(final String key, final boolean b) throws ApplicationException {
        map.put(key, b);
    }

    @Override
    public void remove(final String key) throws ApplicationException {
        map.remove(key);
    }

}
