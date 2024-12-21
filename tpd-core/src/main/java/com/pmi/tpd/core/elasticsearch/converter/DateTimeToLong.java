package com.pmi.tpd.core.elasticsearch.converter;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DateTimeToLong implements Converter<DateTime, Long> {

    @Override
    public Long convert(final DateTime source) {
        return source == null ? null : source.getMillis();
    }
}