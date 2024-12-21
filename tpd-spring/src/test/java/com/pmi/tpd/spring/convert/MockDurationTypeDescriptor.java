package com.pmi.tpd.spring.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.DurationFormat;
import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.api.config.annotation.DurationUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Create a mock {@link TypeDescriptor} with optional {@link DurationUnit @DurationUnit} and
 * {@link DurationFormat @DurationFormat} annotations.
 *
 * @author Phillip Webb
 */
public final class MockDurationTypeDescriptor {

    private MockDurationTypeDescriptor() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypeDescriptor get(final ChronoUnit unit, final DurationStyle style) {
        final TypeDescriptor descriptor = mock(TypeDescriptor.class);
        if (unit != null) {
            final DurationUnit unitAnnotation = AnnotationUtils
                    .synthesizeAnnotation(Collections.singletonMap("value", unit), DurationUnit.class, null);
            given(descriptor.getAnnotation(DurationUnit.class)).willReturn(unitAnnotation);
        }
        if (style != null) {
            final DurationFormat formatAnnotation = AnnotationUtils
                    .synthesizeAnnotation(Collections.singletonMap("value", style), DurationFormat.class, null);
            given(descriptor.getAnnotation(DurationFormat.class)).willReturn(formatAnnotation);
        }
        given(descriptor.getType()).willReturn((Class) Duration.class);
        given(descriptor.getObjectType()).willReturn((Class) Duration.class);
        return descriptor;
    }

}