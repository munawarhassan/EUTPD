package com.pmi.tpd.web.core.rs.jackson.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.EnumValues;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @param <T>
 */
public class JaxbJsonEnumDeserializer<T extends Enum<?>> extends JsonDeserializer<T> {

    /** */
    private final Object locked = new Object();

    /** */
    private Map<SerializableString, T> values;

    /** */
    private final Class<T> enumClass;

    /** */
    private final ObjectMapper objectMapper;

    /**
     * @param enumClass
     * @param objectMapper
     * @return
     */
    public static <E extends Enum<?>> JaxbJsonEnumDeserializer<E> construct(final Class<E> enumClass,
        final ObjectMapper objectMapper) {
        return new JaxbJsonEnumDeserializer<>(enumClass, objectMapper);
    }

    /**
     * @param enumClass
     * @param objectMapper
     */
    public JaxbJsonEnumDeserializer(final Class<T> enumClass, final ObjectMapper objectMapper) {
        this.enumClass = enumClass;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (this.values == null) {
            synchronized (locked) {
                final EnumValues values = EnumValues.construct(this.objectMapper.getSerializationConfig(),
                    (Class<Enum<?>>) enumClass);
                this.values = (Map<SerializableString, T>) reverse(values.internalMap());
            }
        }
        final String v = parser.getValueAsString();
        return values.get(new SerializedString(v));
    }

    /**
     * @param map
     * @return
     */
    public static <K, V> HashMap<V, K> reverse(final Map<K, V> map) {
        final HashMap<V, K> rev = Maps.newHashMap();
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }
        return rev;
    }
}
