package com.pmi.tpd.spring.convert;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.Formatter;
import org.springframework.format.support.FormattingConversionService;

/**
 * Factory for creating a {@link Stream stream} of {@link Arguments} for use in a {@link ParameterizedTest parameterized
 * test}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
final class ConversionServiceArguments {

    private ConversionServiceArguments() {
    }

    static Stream<? extends Arguments> with(final Formatter<?> formatter) {
        return with(conversionService -> conversionService.addFormatter(formatter));
    }

    static Stream<? extends Arguments> with(final GenericConverter converter) {
        return with(conversionService -> conversionService.addConverter(converter));
    }

    static Stream<? extends Arguments> with(final Consumer<FormattingConversionService> initializer) {
        final FormattingConversionService withoutDefaults = new FormattingConversionService();
        initializer.accept(withoutDefaults);
        return Stream.of(
            Arguments.of(new NamedConversionService(withoutDefaults, "Without defaults conversion service")),
            Arguments.of(
                new NamedConversionService(new ApplicationConversionService(), "Application conversion service")));
    }

    static boolean isApplicationConversionService(final ConversionService conversionService) {
        if (conversionService instanceof NamedConversionService) {
            return isApplicationConversionService(((NamedConversionService) conversionService).delegate);
        }
        return conversionService instanceof ApplicationConversionService;
    }

    static class NamedConversionService implements ConversionService {

        private final ConversionService delegate;

        private final String name;

        NamedConversionService(final ConversionService delegate, final String name) {
            this.delegate = delegate;
            this.name = name;
        }

        @Override
        public boolean canConvert(final Class<?> sourceType, final Class<?> targetType) {
            return this.delegate.canConvert(sourceType, targetType);
        }

        @Override
        public boolean canConvert(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
            return this.delegate.canConvert(sourceType, targetType);
        }

        @Override
        public <T> T convert(final Object source, final Class<T> targetType) {
            return this.delegate.convert(source, targetType);
        }

        @Override
        public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
            return this.delegate.convert(source, sourceType, targetType);
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

}