package com.pmi.tpd.web.rest.model;

/**
 * @author Christophe Friederich
 * @since 1.0
 * @param <K>
 *            the type of key
 * @param <V>
 *            the type of value
 */
public class NameValuePair<K, V> {

    /** */
    private final K name;

    /** */
    private final V value;

    /**
     * create new instance {@link NameValuePair}.
     * 
     * @param name
     *            the name associated to value
     * @param value
     *            the value
     * @return Returns a new instance of {@link NameValuePair}.
     */
    public static <K, V> NameValuePair<K, V> create(final K name, final V value) {
        return new NameValuePair<>(name, value);
    }

    /**
     * @param name
     * @param value
     */
    public NameValuePair(final K name, final V value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * @return Returns the name.
     */
    public K getName() {
        return name;
    }

    /**
     * @return Returns the value.
     */
    public V getValue() {
        return value;
    }

}
