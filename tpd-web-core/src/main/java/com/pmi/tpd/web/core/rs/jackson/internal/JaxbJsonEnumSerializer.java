package com.pmi.tpd.web.core.rs.jackson.internal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.EnumValues;

/**
 * @author Christophe Friederich
 * @param <T>
 */
public class JaxbJsonEnumSerializer<T extends Enum<?>> extends JsonSerializer<T> {

    /** */
    private final Object locked = new Object();

    /**
     * This map contains pre-resolved values (since there are ways to customize actual String constants to use) to use
     * as serializations.
     */
    private EnumValues values;

    /** */
    private final Class<T> enumClass;

    /** */
    private final ObjectMapper objectMapper;

    /**
     * @param enumClass
     * @param objectMapper
     * @return
     */
    public static <E extends Enum<?>> JaxbJsonEnumSerializer<E> construct(final Class<E> enumClass,
        final ObjectMapper objectMapper) {
        return new JaxbJsonEnumSerializer<>(enumClass, objectMapper);
    }

    /**
     * @param enumClass
     * @param objectMapper
     */
    public JaxbJsonEnumSerializer(final Class<T> enumClass, final ObjectMapper objectMapper) {
        this.enumClass = enumClass;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final T value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        if (this.values == null) {
            synchronized (locked) {
                this.values = EnumValues.construct(this.objectMapper.getSerializationConfig(),
                    (Class<Enum<?>>) enumClass);
            }
        }
        gen.writeString(values.serializedValueFor(value));

    }

}
