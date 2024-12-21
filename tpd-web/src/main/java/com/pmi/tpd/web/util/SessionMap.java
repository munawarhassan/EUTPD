package com.pmi.tpd.web.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.google.common.collect.Sets;

public class SessionMap<V> implements Map<String, V> {

    /** The internal session attribute. */
    protected HttpSession session;

    /**
     * Create a <tt>HttpSession</tt> <tt>Map</tt> adaptor.
     *
     * @param value
     *            the http session
     */
    public SessionMap(final HttpSession value) {
        session = value;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        if (session != null) {
            int size = 0;
            final Enumeration<String> enumeration = session.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                enumeration.nextElement();
                size++;
            }
            return size;
        } else {
            return 0;
        }
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @see java.util.Map#containsKey(Object)
     */
    @Override
    public boolean containsKey(final Object key) {
        if (session != null && key != null) {
            return session.getAttribute(key.toString()) != null;
        } else {
            return false;
        }
    }

    /**
     * This method is not supported and will throw <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#containsValue(Object)
     */
    @Override
    public boolean containsValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * If the stored object is a FlashObject this method will return the FlashObject value and then remove it from the
     * session.
     *
     * @see java.util.Map#get(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(final Object key) {
        if (session != null && key != null) {
            final Object object = session.getAttribute(key.toString());
            return (V) object;

        } else {
            return null;
        }
    }

    /**
     * @see java.util.Map#put(Object, Object)
     */
    @Override
    public V put(final String key, final V value) {
        if (session != null && key != null) {
            @SuppressWarnings("unchecked")
            final V out = (V) session.getAttribute(key);

            session.setAttribute(key.toString(), value);

            return out;

        } else {
            return null;
        }
    }

    /**
     * @see java.util.Map#remove(Object)
     */
    @Override
    public V remove(final Object key) {
        if (session != null && key != null) {
            @SuppressWarnings("unchecked")
            final V out = (V) session.getAttribute(key.toString());
            session.removeAttribute(key.toString());

            return out;

        } else {
            return null;
        }
    }

    @Override
    public void putAll(final Map<? extends String, ? extends V> map) {
        if (session != null && map != null) {
            for (final String key : map.keySet()) {
                session.setAttribute(key, map.get(key));
            }
        }
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        if (session != null) {
            final Enumeration<String> enumeration = session.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                final String name = enumeration.nextElement().toString();
                session.removeAttribute(name);
            }
        }
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        if (session != null) {
            final Set<String> keySet = Sets.newHashSet();

            final Enumeration<String> enumeration = session.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                keySet.add(enumeration.nextElement());
            }

            return keySet;

        } else {
            return Collections.emptySet();
        }
    }

    /**
     * This method is not supported and will throw <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        if (session != null) {
            final Set<Map.Entry<String, V>> entrySet = Sets.newHashSet();

            final Enumeration<String> enumeration = session.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                final EntryImpl<String, V> entry = new EntryImpl<>();
                entry.key = enumeration.nextElement().toString();
                entry.value = (V) session.getAttribute(entry.key);
                entrySet.add(entry);
            }

            return entrySet;

        } else {
            return Collections.emptySet();
        }
    }

    /**
     * @author Christophe Friederich
     * @param <K>
     * @param <V>
     */
    private static final class EntryImpl<K, V> implements Entry<K, V> {

        /**
         * Holds the entry key (null when in pool).
         */
        private K key;

        /**
         * Holds the entry value (null when in pool).
         */
        private V value;

        /**
         * Returns the key for this entry.
         *
         * @return the entry's key.
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * Returns the value for this entry.
         *
         * @return the entry's value.
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * Sets the value for this entry.
         *
         * @param value
         *            the new value.
         * @return the previous value.
         */
        @Override
        public V setValue(final V value) {
            final V old = this.value;
            this.value = value;
            return old;
        }

        /**
         * Indicates if this entry is considered equals to the specified entry.
         *
         * @param that
         *            the object to test for equality.
         * @return <code>true</code> if both entry are considered equal; <code>false</code> otherwise.
         */
        @Override
        public boolean equals(final Object that) {
            if (that instanceof Map.Entry) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final Map.Entry<K, V> entry = (Map.Entry) that;
                return key.equals(entry.getKey())
                        && (value != null ? value.equals(entry.getValue()) : entry.getValue() == null);
            } else {
                return false;
            }
        }

        /**
         * Returns the hash code for this entry.
         *
         * @return this entry's hash code.
         */
        @Override
        public int hashCode() {
            return key.hashCode() ^ (value != null ? value.hashCode() : 0);
        }

        /**
         * Returns the text representation of this entry.
         *
         * @return this entry's textual representation.
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

}
