package com.pmi.tpd.jackson;

import java.io.IOException;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * ISO 8601 date format Jackson deserializer for displaying Joda DateTime objects.
 */
public class ISO8601LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            final String str = jp.getText().trim();
            return ISODateTimeFormat.dateTimeParser().parseLocalDate(str);
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new LocalDate(jp.getLongValue());
        }
        return (LocalDate)ctxt.handleUnexpectedToken(handledType(), jp);
    }
}
