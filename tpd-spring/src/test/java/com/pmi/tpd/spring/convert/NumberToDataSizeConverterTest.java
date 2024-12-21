package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import com.pmi.tpd.api.config.annotation.DataSizeUnit;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests for {@link NumberToDataSizeConverter}.
 *
 * @author Stephane Nicoll
 */
class NumberToDataSizeConverterTest extends MockitoTestCase {

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnDataSize(final ConversionService conversionService) {
        assertThat(convert(conversionService, 10), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, +10), is(DataSize.ofBytes(10)));
        assertThat(convert(conversionService, -10), is(DataSize.ofBytes(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnDataSize(
        final ConversionService conversionService) {
        assertThat(convert(conversionService, 10, DataUnit.KILOBYTES), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, +10, DataUnit.KILOBYTES), is(DataSize.ofKilobytes(10)));
        assertThat(convert(conversionService, -10, DataUnit.KILOBYTES), is(DataSize.ofKilobytes(-10)));
    }

    private DataSize convert(final ConversionService conversionService, final Integer source) {
        return conversionService.convert(source, DataSize.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DataSize convert(final ConversionService conversionService,
        final Integer source,
        final DataUnit defaultUnit) {
        final TypeDescriptor targetType = mock(TypeDescriptor.class);
        if (defaultUnit != null) {
            final DataSizeUnit unitAnnotation = AnnotationUtils
                    .synthesizeAnnotation(Collections.singletonMap("value", defaultUnit), DataSizeUnit.class, null);
            when(targetType.getAnnotation(DataSizeUnit.class)).thenReturn(unitAnnotation);
        }
        when(targetType.getType()).thenReturn((Class) DataSize.class);
        return (DataSize) conversionService.convert(source, TypeDescriptor.forObject(source), targetType);
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new NumberToDataSizeConverter());
    }

}