package com.pmi.tpd.spring.convert;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link ApplicationConversionService}.
 *
 * @author Phillip Webb
 */
class ApplicationConversionServiceTest extends TestCase {

    private final FormatterRegistry registry = mock(FormatterRegistry.class);

    @Test
    void addBeansWhenHasGenericConverterBeanAddConverter() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
                ExampleGenericConverter.class)) {
            ApplicationConversionService.addBeans(this.registry, context);
            verify(this.registry).addConverter(context.getBean(ExampleGenericConverter.class));
            verifyNoMoreInteractions(this.registry);
        }
    }

    @Test
    void addBeansWhenHasConverterBeanAddConverter() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ExampleConverter.class)) {
            ApplicationConversionService.addBeans(this.registry, context);
            verify(this.registry).addConverter(context.getBean(ExampleConverter.class));
            verifyNoMoreInteractions(this.registry);
        }
    }

    @Test
    void addBeansWhenHasFormatterBeanAddsOnlyFormatter() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ExampleFormatter.class)) {
            ApplicationConversionService.addBeans(this.registry, context);
            verify(this.registry).addFormatter(context.getBean(ExampleFormatter.class));
            verifyNoMoreInteractions(this.registry);
        }
    }

    @Test
    void addBeansWhenHasPrinterBeanAddPrinter() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ExamplePrinter.class)) {
            ApplicationConversionService.addBeans(this.registry, context);
            verify(this.registry).addPrinter(context.getBean(ExamplePrinter.class));
            verifyNoMoreInteractions(this.registry);
        }
    }

    @Test
    void addBeansWhenHasParserBeanAddParser() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ExampleParser.class)) {
            ApplicationConversionService.addBeans(this.registry, context);
            verify(this.registry).addParser(context.getBean(ExampleParser.class));
            verifyNoMoreInteractions(this.registry);
        }
    }

    @Test
    void isConvertViaObjectSourceTypeWhenObjectSourceReturnsTrue() {
        // Uses ObjectToCollectionConverter
        final ApplicationConversionService conversionService = new ApplicationConversionService();
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(Long.class);
        final TypeDescriptor targetType = TypeDescriptor.valueOf(List.class);
        assertThat(conversionService.canConvert(sourceType, targetType), is(true));
        assertThat(conversionService.isConvertViaObjectSourceType(sourceType, targetType), is(true));
    }

    @Test
    void isConvertViaObjectSourceTypeWhenNotObjectSourceReturnsFalse() {
        // Uses StringToCollectionConverter
        final ApplicationConversionService conversionService = new ApplicationConversionService();
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor.valueOf(List.class);
        assertThat(conversionService.canConvert(sourceType, targetType), is(true));
        assertThat(conversionService.isConvertViaObjectSourceType(sourceType, targetType), is(false));
    }

    // @Test
    // void sharedInstanceCannotBeModified() {
    // final ApplicationConversionService instance = (ApplicationConversionService) ApplicationConversionService
    // .getSharedInstance();
    // assertUnmodifiableExceptionThrown(() -> instance.addPrinter(null));
    // assertUnmodifiableExceptionThrown(() -> instance.addParser(null));
    // assertUnmodifiableExceptionThrown(() -> instance.addFormatter(null));
    // assertUnmodifiableExceptionThrown(() -> instance.addFormatterForFieldType(null, null));
    // assertUnmodifiableExceptionThrown(() -> instance.addConverter((Converter<?, ?>) null));
    // assertUnmodifiableExceptionThrown(() -> instance.addFormatterForFieldType(null, null, null));
    // assertUnmodifiableExceptionThrown(() -> instance.addFormatterForFieldAnnotation(null));
    // assertUnmodifiableExceptionThrown(() -> instance.addConverter(null, null, null));
    // assertUnmodifiableExceptionThrown(() -> instance.addConverter((GenericConverter) null));
    // assertUnmodifiableExceptionThrown(() -> instance.addConverterFactory(null));
    // assertUnmodifiableExceptionThrown(() -> instance.removeConvertible(null, null));
    // }
    //
    // private void assertUnmodifiableExceptionThrown(final ThrowingCallable throwingCallable) {
    // assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(throwingCallable)
    // .withMessage("This ApplicationConversionService cannot be modified");
    // }

    static class ExampleGenericConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
            return null;
        }

    }

    static class ExampleConverter implements Converter<String, Integer> {

        @Override
        public Integer convert(final String source) {
            return null;
        }

    }

    static class ExampleFormatter implements Formatter<Integer> {

        @Override
        public String print(final Integer object, final Locale locale) {
            return null;
        }

        @Override
        public Integer parse(final String text, final Locale locale) throws ParseException {
            return null;
        }

    }

    static class ExampleParser implements Parser<Integer> {

        @Override
        public Integer parse(final String text, final Locale locale) throws ParseException {
            return null;
        }

    }

    static class ExamplePrinter implements Printer<Integer> {

        @Override
        public String print(final Integer object, final Locale locale) {
            return null;
        }

    }

}