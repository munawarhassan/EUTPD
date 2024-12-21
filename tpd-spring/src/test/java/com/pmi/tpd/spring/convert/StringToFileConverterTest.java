package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link StringToFileConverter}.
 *
 * @author Phillip Webb
 */
class StringToFileConverterTest extends TestCase {

    @TempDir
    File temp;

    @ConversionServiceTest
    void convertWhenSimpleFileReturnsFile(final ConversionService conversionService) {
        assertThat(convert(conversionService, this.temp.getAbsolutePath() + "/test"),
            is(new File(this.temp, "test").getAbsoluteFile()));
    }

    @ConversionServiceTest
    void convertWhenFilePrefixedReturnsFile(final ConversionService conversionService) {
        assertThat(convert(conversionService, "file:" + this.temp.getAbsolutePath() + "/test").getAbsoluteFile(),
            is(new File(this.temp, "test").getAbsoluteFile()));
    }

    private File convert(final ConversionService conversionService, final String source) {
        return conversionService.convert(source, File.class);
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(conversionService -> conversionService.addConverter(new StringToFileConverter()));
    }

}