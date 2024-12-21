package com.pmi.tpd.core.elasticsearch.converter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LongToLocalDate implements Converter<Long, LocalDate> {

    @Override
    public LocalDate convert(final Long source) {
        return source == null ? null : new DateTime(source).toLocalDate();
    }
}