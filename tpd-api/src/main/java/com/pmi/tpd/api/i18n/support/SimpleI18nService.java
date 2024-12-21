package com.pmi.tpd.api.i18n.support;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import com.google.common.base.Joiner;
import com.pmi.tpd.api.i18n.I18nKey;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * A simplified implementation of the {@link I18nService} which uses {@code MessageFormat} to directly format the
 * fallback messages, rather than attempting to perform any real internationalisation.
 * <p>
 * This implementation has been built purely to support testing; it is not intended for use in production code.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleI18nService implements I18nService {

    /** */
    private final Mode mode;

    /**
     * Constructs a {@code SimpleI18nService} which will format arguments into the fallback message and return it.
     *
     * @see Mode#FORMAT_MESSAGES
     */
    public SimpleI18nService() {
        this(Mode.RETURN_KEYS);
    }

    /**
     * Constructs a {@code SimpleI18nService} which will use the specified {@link Mode mode} for handling messages.
     *
     * @param mode
     *             the mode to use.
     * @see Mode
     */
    public SimpleI18nService(final Mode mode) {
        this.mode = mode;
    }

    @Override
    @Nonnull
    public KeyedMessage createKeyedMessage(@Nonnull final String key, @Nonnull final Object... arguments) {
        return getKeyedText(key, null, arguments);
    }

    @Override
    @Nonnull
    public String getMessage(@Nonnull final String key, @Nonnull final Object... arguments) {
        return getText(key, null, arguments);
    }

    @Nonnull
    @Override
    public String getMessage(@Nonnull final I18nKey key) {
        return getText(key.getKey(), null, key.getArguments());
    }

    @Override
    @Nonnull
    public Map<String, String> getAllTranslationsForPrefix(@Nonnull final String prefix) {
        return Collections.emptyMap();
    }

    @Override
    @Nonnull
    public Map<String, String> getAllTranslationsForPrefix(@Nonnull final String prefix, @Nonnull final Locale locale) {
        return getAllTranslationsForPrefix(prefix);
    }

    @Override
    @Nonnull
    public KeyedMessage getKeyedText(@Nonnull final String key,
        final @Nullable String fallbackMessage,
        @Nonnull final Object... arguments) {
        final String message = getText(key, fallbackMessage, arguments);

        return new KeyedMessage(key, message, message);
    }

    @Override
    public String getMessagePattern(@Nonnull final String key) {
        return getMessagePattern(key, null);
    }

    @Override
    public String getMessagePattern(@Nonnull final String key, final @Nullable String fallbackPattern) {
        return fallbackPattern;
    }

    @Override
    public String getMessagePattern(@Nonnull final Locale locale, @Nonnull final String key) {
        return getMessagePattern(key, null);
    }

    @Override
    @Nonnull
    public KeyedMessage getKeyedText(@Nonnull final I18nKey i18nKey) {
        return createKeyedMessage(i18nKey.getKey(), i18nKey.getArguments());
    }

    @Override
    @Nonnull
    public String getText(@Nonnull final Locale locale,
        @Nonnull final String key,
        final @Nullable String fallbackMessage,
        @Nonnull final Object... arguments) {
        return getText(key, fallbackMessage, arguments);
    }

    @Override
    @Nonnull
    public String getText(@Nonnull final String key,
        final @Nullable String fallbackMessage,
        @Nonnull final Object... arguments) {
        return mode.apply(key, fallbackMessage, arguments);
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
        return getText(code, null, args);
    }

    @Override
    public String getMessage(final String code, final Object[] args, final Locale locale)
            throws NoSuchMessageException {
        return getText(code, null, args);
    }

    @Override
    public String getMessage(final MessageSourceResolvable resolvable, final Locale locale)
            throws NoSuchMessageException {

        return getText(resolvable.getCodes()[0], null, resolvable.getArguments());
    }

    /**
     * Enumerates the possible modes for the {@link SimpleI18nService}.
     */
    public enum Mode {

        /**
         * Uses {@code MessageFormat} to format all arguments into the fallback message and return the result.
         */
        FORMAT_MESSAGES {

            @Override
            public @Nonnull String apply(final String key, final String fallbackMessage, final Object... arguments) {
                return MessageFormat.format(fallbackMessage, arguments);
            }
        },
        /**
         * Returns the message key, which can be useful in verifying that the expected message is being looked up.
         */
        RETURN_KEYS {

            @Override
            public @Nonnull String apply(final String key, final String fallbackMessage, final Object... arguments) {
                return key;
            }
        },
        /**
         * Returns the message key and the arguments, which can be useful in verifying that the expected message is
         * being looked up with the expected arguments.
         */
        RETURN_KEYS_WITH_ARGUMENTS {

            @Override
            public @Nonnull String apply(final String key, final String fallbackMessage, final Object... arguments) {
                if (arguments.length == 0) {
                    return key;
                }
                return key + "(" + Joiner.on(", ").join(arguments) + ")";
            }
        },
        /**
         * Returns the fallback message <i>without</i> formatting any arguments into it. Placeholders are left intact,
         * which can be useful in verifying format strings.
         */
        RETURN_MESSAGES {

            @Override
            public @Nonnull String apply(final String key, final String fallbackMessage, final Object... arguments) {
                return fallbackMessage;
            }
        };

        /**
         * Format the fallback message corresponding each mode.
         *
         * @param key
         *                        the key.
         * @param fallbackMessage
         *                        the fallback message to use.
         * @param arguments
         *                        list of arguments to use.
         * @return Returns a {@link String} representing the fallback message according to mode used.
         */
        public abstract @Nonnull String apply(String key, String fallbackMessage, Object... arguments);
    }

}
