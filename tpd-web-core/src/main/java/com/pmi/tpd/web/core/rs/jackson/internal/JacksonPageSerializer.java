package com.pmi.tpd.web.core.rs.jackson.internal;

import java.io.IOException;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JacksonPageSerializer<T> extends JsonSerializer<PageImpl<T>> {

    public static JacksonPageSerializer<PageImpl<?>> construct() {
        return new JacksonPageSerializer<>();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<PageImpl<T>> handledType() {
        return (Class) PageImpl.class;
    }

    @Override
    public void serialize(final PageImpl<T> page,
        final JsonGenerator jsonGenerator,
        final SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("content", page.getContent());
        jsonGenerator.writeBooleanField("first", page.isFirst());
        jsonGenerator.writeBooleanField("last", page.isLast());
        jsonGenerator.writeNumberField("totalPages", page.getTotalPages());
        jsonGenerator.writeNumberField("totalElements", page.getTotalElements());
        jsonGenerator.writeNumberField("numberOfElements", page.getNumberOfElements());
        jsonGenerator.writeNumberField("size", page.getSize());
        jsonGenerator.writeNumberField("number", page.getNumber());
        jsonGenerator.writeBooleanField("empty", page.isEmpty());

        final Sort sort = page.getSort();
        jsonGenerator.writeArrayFieldStart("sort");

        for (final Sort.Order order : sort) {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("property", order.getProperty());
            jsonGenerator.writeStringField("direction", order.getDirection().name());
            jsonGenerator.writeBooleanField("ignoreCase", order.isIgnoreCase());
            jsonGenerator.writeStringField("nullHandling", order.getNullHandling().name());
            jsonGenerator.writeBooleanField("ascending", order.isAscending());
            jsonGenerator.writeBooleanField("descending", order.isDescending());
            jsonGenerator.writeEndObject();

        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();

    }

}