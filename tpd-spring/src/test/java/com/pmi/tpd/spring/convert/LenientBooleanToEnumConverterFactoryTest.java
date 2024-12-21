package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.equalTo;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link LenientBooleanToEnumConverterFactory}.
 *
 * @author Madhura Bhave
 */
class LenientBooleanToEnumConverterFactoryTest extends TestCase {

    @ConversionServiceTest
    void convertFromBooleanToEnumWhenShouldConvertValue(final ConversionService conversionService) {
        assertThat(conversionService.convert(true, TestOnOffEnum.class), equalTo(TestOnOffEnum.ON));
        assertThat(conversionService.convert(false, TestOnOffEnum.class), equalTo(TestOnOffEnum.OFF));
        assertThat(conversionService.convert(true, TestTrueFalseEnum.class), equalTo(TestTrueFalseEnum.TRUE));
        assertThat(conversionService.convert(false, TestTrueFalseEnum.class), equalTo(TestTrueFalseEnum.FALSE));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(service -> service.addConverterFactory(new LenientBooleanToEnumConverterFactory()));
    }

    enum TestOnOffEnum {
        ON,
        OFF
    }

    enum TestTrueFalseEnum {

        ONE,
        TWO,
        TRUE,
        FALSE,
        ON,
        OFF

    }

}