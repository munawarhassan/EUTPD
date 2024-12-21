package com.pmi.tpd.core.elasticsearch.converter;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LongToDateTime implements Converter<Long, DateTime> {

    @Override
    public DateTime convert(final Long source) {
        return source == null ? null : new DateTime(source);
    }
}