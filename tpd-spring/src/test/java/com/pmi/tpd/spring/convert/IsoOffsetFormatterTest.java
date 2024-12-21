package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link IsoOffsetFormatter}.
 *
 * @author Phillip Webb
 */
class IsoOffsetFormatterTest extends TestCase {

    @ConversionServiceTest
    void convertShouldConvertStringToIsoDate(final ConversionService conversionService) {
        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime converted = conversionService.convert(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now),
            OffsetDateTime.class);
        assertThat(converted, equalTo(now));
    }

    @ConversionServiceTest
    void convertShouldConvertIsoDateToString(final ConversionService conversionService) {
        final OffsetDateTime now = OffsetDateTime.now();
        final String converted = conversionService.convert(now, String.class);
        assertThat(converted, anyOf(notNullValue(), startsWith(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new IsoOffsetFormatter());
    }

}