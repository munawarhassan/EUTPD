package com.pmi.tpd.spring.convert;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

/**
 * Tests for {@link CharArrayFormatter}.
 *
 * @author Phillip Webb
 */
class CharArrayFormatterTest extends TestCase {

    @ConversionServiceTest
    void convertFromCharArrayToStringShouldConvert(final ConversionService conversionService) {
        final char[] source = { 'b', 'o', 'o', 't' };
        final String converted = conversionService.convert(source, String.class);
        assertThat(converted, equalTo("boot"));
    }

    @ConversionServiceTest
    void convertFromStringToCharArrayShouldConvert(final ConversionService conversionService) {
        final String source = "boot";
        final char[] converted = conversionService.convert(source, char[].class);
        assertThat(converted, is(new char[] { 'b', 'o', 'o', 't' }));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new CharArrayFormatter());
    }

}