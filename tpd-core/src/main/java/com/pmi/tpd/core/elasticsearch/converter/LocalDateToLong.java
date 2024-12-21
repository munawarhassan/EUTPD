package com.pmi.tpd.core.elasticsearch.converter;

import org.joda.time.LocalDate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class LocalDateToLong implements Converter<LocalDate, Long> {

    @Override
    public Long convert(final LocalDate source) {
        return source == null ? null : source.toDateTimeAtStartOfDay().getMillis();
    }
}