package com.pmi.tpd.jackson;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {

    /** */
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public void serialize(@Nullable final LocalDate value,
        @Nonnull final JsonGenerator jgen,
        @Nullable final SerializerProvider provider) throws IOException {
        jgen.writeString(FORMATTER.print(value));
    }
}
