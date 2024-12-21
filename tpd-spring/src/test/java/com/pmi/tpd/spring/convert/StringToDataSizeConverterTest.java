package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link StringToDataSizeConverter}.
 *
 * @author Stephane Nicoll
 */
class StringToDataSizeConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWhenSimpleBytesShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10B"), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, "+10B"), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, "-10B"), is(DataSize.ofBytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleKilobytesShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10KB"), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, "+10KB"), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, "-10KB"), is(DataSize.ofKilobytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleMegabytesShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10MB"), is(DataSize.ofMegabytes(10)));
        assertThat(convert(conversionService, "+10MB"), is(DataSize.ofMegabytes(10)));
        assertThat(convert(conversionService, "-10MB"), is(DataSize.ofMegabytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleGigabytesShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10GB"), is(DataSize.ofGigabytes(10)));
        assertThat(convert(conversionService, "+10GB"), is(DataSize.ofGigabytes(10)));
        assertThat(convert(conversionService, "-10GB"), is(DataSize.ofGigabytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleTerabytesShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10TB"), is(DataSize.ofTerabytes(10)));
        assertThat(convert(conversionService, "+10TB"), is(DataSize.ofTerabytes(10)));
        assertThat(convert(conversionService, "-10TB"), is(DataSize.ofTerabytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10"), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, "+10"), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, "-10"), is(DataSize.ofBytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnDataSize(
        final ConversionService conversionService) {
        assertThat(convert(conversionService, "10", DataUnit.KILOBYTES), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, "+10", DataUnit.KILOBYTES), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, "-10", DataUnit.KILOBYTES), is(DataSize.ofKilobytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenBadFormatShouldThrowException(final ConversionService conversionService) {
        assertThrows(ConversionFailedException.class,
            () -> convert(conversionService, "10WB"),
            "'10WB' is not a valid data size");
    }

    @ConversionServiceTest
    void convertWhenEmptyShouldReturnNull(final ConversionService conversionService) {
        assertThat(convert(conversionService, ""), nullValue());
    }

    private DataSize convert(final ConversionService conversionService, final String source) {
        return conversionService.convert(source, DataSize.class);
    }

    private DataSize convert(final ConversionService conversionService, final String source, final DataUnit unit) {
        return (DataSize) conversionService
                .convert(source, TypeDescriptor.forObject(source), MockDataSizeTypeDescriptor.get(unit));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new StringToDataSizeConverter());
    }

}