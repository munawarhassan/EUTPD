package com.pmi.tpd.spring.convert;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

/**
 * {@link ConditionalGenericConverter} to convert {@link CharSequence} type by delegating to existing {@link String}
 * converters.
 *
 * @author Phillip Webb
 */
class CharSequenceToObjectConverter implements ConditionalGenericConverter {

    private static final TypeDescriptor STRING = TypeDescriptor.valueOf(String.class);

    private static final TypeDescriptor BYTE_ARRAY = TypeDescriptor.valueOf(byte[].class);

    private static final Set<ConvertiblePair> TYPES;

    private final ThreadLocal<Boolean> disable = new ThreadLocal<>();

    static {
        TYPES = Collections.singleton(new ConvertiblePair(CharSequence.class, Object.class));
    }

    private final ConversionService conversionService;

    CharSequenceToObjectConverter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return TYPES;
    }

    @Override
    public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (sourceType.getType() == String.class || this.disable.get() == Boolean.TRUE) {
            return false;
        }
        this.disable.set(Boolean.TRUE);
        try {
            final boolean canDirectlyConvertCharSequence = this.conversionService.canConvert(sourceType, targetType);
            if (canDirectlyConvertCharSequence && !isStringConversionBetter(sourceType, targetType)) {
                return false;
            }
            return this.conversionService.canConvert(STRING, targetType);
        } finally {
            this.disable.set(null);
        }
    }

    /**
     * Return if String based conversion is better based on the target type. This is required when ObjectTo...
     * conversion produces incorrect results.
     *
     * @param sourceType
     *            the source type to test
     * @param targetType
     *            the target type to test
     * @return if string conversion is better
     */
    private boolean isStringConversionBetter(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (this.conversionService instanceof ApplicationConversionService) {
            final ApplicationConversionService applicationConversionService = (ApplicationConversionService) this.conversionService;
            if (applicationConversionService.isConvertViaObjectSourceType(sourceType, targetType)) {
                // If an ObjectTo... converter is being used then there might be a better
                // StringTo... version
                return true;
            }
        }
        if ((targetType.isArray() || targetType.isCollection()) && !targetType.equals(BYTE_ARRAY)) {
            // StringToArrayConverter / StringToCollectionConverter are better than
            // ObjectToArrayConverter / ObjectToCollectionConverter
            return true;
        }
        return false;
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return this.conversionService.convert(source.toString(), STRING, targetType);
    }

}