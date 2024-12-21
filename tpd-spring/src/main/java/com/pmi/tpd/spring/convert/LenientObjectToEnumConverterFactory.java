package com.pmi.tpd.spring.convert;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Abstract base class for converting from a type to a {@link java.lang.Enum}.
 *
 * @param <T>
 *            the source type
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@SuppressWarnings("rawtypes")
abstract class LenientObjectToEnumConverterFactory<T> implements ConverterFactory<T, Enum<?>> {

    private static Map<String, List<String>> ALIASES;

    static {
        final MultiValueMap<String, String> aliases = new LinkedMultiValueMap<>();
        aliases.add("true", "on");
        aliases.add("false", "off");
        ALIASES = Collections.unmodifiableMap(aliases);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Enum<?>> Converter<T, E> getConverter(final Class<E> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
        return new LenientToEnumConverter<>((Class<E>) enumType);
    }

    @SuppressWarnings("unchecked")
    private class LenientToEnumConverter<E extends Enum> implements Converter<T, E> {

        private final Class<E> enumType;

        LenientToEnumConverter(final Class<E> enumType) {
            this.enumType = enumType;
        }

        @Override
        public E convert(final T source) {
            final String value = source.toString().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return (E) Enum.valueOf(this.enumType, value);
            } catch (final Exception ex) {
                return findEnum(value);
            }
        }

        private E findEnum(final String value) {
            final String name = getCanonicalName(value);
            final List<String> aliases = ALIASES.getOrDefault(name, Collections.emptyList());
            for (final E candidate : (Set<E>) EnumSet.allOf(this.enumType)) {
                final String candidateName = getCanonicalName(candidate.name());
                if (name.equals(candidateName) || aliases.contains(candidateName)) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException("No enum constant " + this.enumType.getCanonicalName() + "." + value);
        }

        private String getCanonicalName(final String name) {
            final StringBuilder canonicalName = new StringBuilder(name.length());
            name.chars()
                    .filter(Character::isLetterOrDigit)
                    .map(Character::toLowerCase)
                    .forEach(c -> canonicalName.append((char) c));
            return canonicalName.toString();
        }

    }

}