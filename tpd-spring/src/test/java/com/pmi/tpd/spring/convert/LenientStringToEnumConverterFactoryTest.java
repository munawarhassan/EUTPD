package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link LenientStringToEnumConverterFactory}.
 *
 * @author Phillip Webb
 */
class LenientStringToEnumConverterFactoryTest extends TestCase {

    @ConversionServiceTest
    void canConvertFromStringToEnumShouldReturnTrue(final ConversionService conversionService) {
        assertThat(conversionService.canConvert(String.class, TestEnum.class), is(true));
    }

    @ConversionServiceTest
    void canConvertFromStringToEnumSubclassShouldReturnTrue(final ConversionService conversionService) {
        assertThat(conversionService.canConvert(String.class, TestSubclassEnum.ONE.getClass()), is(true));
    }

    @ConversionServiceTest
    void convertFromStringToEnumWhenExactMatchShouldConvertValue(final ConversionService conversionService) {
        assertThat(conversionService.convert("", TestEnum.class), nullValue());
        assertThat(conversionService.convert("ONE", TestEnum.class), is(TestEnum.ONE));
        assertThat(conversionService.convert("TWO", TestEnum.class), is(TestEnum.TWO));
        assertThat(conversionService.convert("THREE_AND_FOUR", TestEnum.class), is(TestEnum.THREE_AND_FOUR));
    }

    @ConversionServiceTest
    void convertFromStringToEnumWhenFuzzyMatchShouldConvertValue(final ConversionService conversionService) {
        assertThat(conversionService.convert("", TestEnum.class), nullValue());
        assertThat(conversionService.convert("one", TestEnum.class), is(TestEnum.ONE));
        assertThat(conversionService.convert("tWo", TestEnum.class), is(TestEnum.TWO));
        assertThat(conversionService.convert("three_and_four", TestEnum.class), is(TestEnum.THREE_AND_FOUR));
        assertThat(conversionService.convert("threeandfour", TestEnum.class), is(TestEnum.THREE_AND_FOUR));
        assertThat(conversionService.convert("three-and-four", TestEnum.class), is(TestEnum.THREE_AND_FOUR));
        assertThat(conversionService.convert("threeAndFour", TestEnum.class), is(TestEnum.THREE_AND_FOUR));
    }

    @ConversionServiceTest
    void convertFromStringToEnumWhenUsingNonEnglishLocaleShouldConvertValue(final ConversionService conversionService) {
        final Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            final LocaleSensitiveEnum result = conversionService.convert("accept-case-insensitive-properties",
                LocaleSensitiveEnum.class);
            assertThat(result, is(LocaleSensitiveEnum.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @ConversionServiceTest
    void convertFromStringToEnumWhenYamlBooleanShouldConvertValue(final ConversionService conversionService) {
        assertThat(conversionService.convert("one", TestOnOffEnum.class), is(TestOnOffEnum.ONE));
        assertThat(conversionService.convert("two", TestOnOffEnum.class), is(TestOnOffEnum.TWO));
        assertThat(conversionService.convert("true", TestOnOffEnum.class), is(TestOnOffEnum.ON));
        assertThat(conversionService.convert("false", TestOnOffEnum.class), is(TestOnOffEnum.OFF));
        assertThat(conversionService.convert("TRUE", TestOnOffEnum.class), is(TestOnOffEnum.ON));
        assertThat(conversionService.convert("FALSE", TestOnOffEnum.class), is(TestOnOffEnum.OFF));
        assertThat(conversionService.convert("fA_lsE", TestOnOffEnum.class), is(TestOnOffEnum.OFF));
        assertThat(conversionService.convert("one", TestTrueFalseEnum.class), is(TestTrueFalseEnum.ONE));
        assertThat(conversionService.convert("two", TestTrueFalseEnum.class), is(TestTrueFalseEnum.TWO));
        assertThat(conversionService.convert("true", TestTrueFalseEnum.class), is(TestTrueFalseEnum.TRUE));
        assertThat(conversionService.convert("false", TestTrueFalseEnum.class), is(TestTrueFalseEnum.FALSE));
        assertThat(conversionService.convert("TRUE", TestTrueFalseEnum.class), is(TestTrueFalseEnum.TRUE));
        assertThat(conversionService.convert("FALSE", TestTrueFalseEnum.class), is(TestTrueFalseEnum.FALSE));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(service -> service.addConverterFactory(new LenientStringToEnumConverterFactory()));
    }

    enum TestEnum {

        ONE,
        TWO,
        THREE_AND_FOUR

    }

    enum TestOnOffEnum {

        ONE,
        TWO,
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

    enum LocaleSensitiveEnum {

        ACCEPT_CASE_INSENSITIVE_PROPERTIES

    }

    enum TestSubclassEnum {

        ONE {

            @Override
            public String toString() {
                return "foo";
            }

        }

    }

}