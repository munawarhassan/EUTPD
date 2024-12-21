package com.pmi.tpd.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Assertion utility class that assists in validating arguments. Useful for identifying programmer errors early and
 * clearly at runtime.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class Assert {

    /**
     *
     */
    private Assert() {
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
     * </pre>
     *
     * @param expression
     *                   a boolean expression
     * @param message
     *                   the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if expression is <code>false</code>
     */
    public static void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0);
     * </pre>
     *
     * @param expression
     *                   a boolean expression
     * @throws java.lang.IllegalArgumentException
     *                                            if expression is <code>false</code>
     */
    public static void isTrue(final boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Assert that an object is <code>null</code> .
     *
     * <pre class="code">
     * Assert.isNull(value, &quot;The value must be null&quot;);
     * </pre>
     *
     * @param object
     *                the object to check
     * @param message
     *                the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is not <code>null</code>
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    public static <T> T isNull(final T object, final String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Assert that an object is <code>null</code> .
     *
     * <pre class="code">
     * Assert.isNull(value);
     * </pre>
     *
     * @param object
     *               the object to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is not <code>null</code>
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    public static <T> T isNull(final T object) {
        return isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * <p>
     * checkNotNull.
     * </p>
     *
     * @param object
     *                      a T object.
     * @param parameterName
     *                      a {@link java.lang.String} object.
     * @param <T>
     *                      a T object.
     * @return a T object.
     */
    @Nonnull
    public static <T> T checkNotNull(@Nullable final T object, @Nonnull final String parameterName) {
        if (object == null) {
            throw new IllegalArgumentException(required(parameterName));
        }
        return object;
    }

    /**
     * @param object
     * @param parameterName
     * @return
     * @since 2.0
     */
    @Nonnull
    public static String checkNotEmpty(@Nullable final String object, @Nonnull final String parameterName) {
        if (isNotEmpty(object)) {
            throw new IllegalArgumentException(required(parameterName));
        }
        return object;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference
     *                             an object reference
     * @param errorMessageTemplate
     *                             a template for the exception message should the check fail. The message is formed by
     *                             replacing each {@code %s} placeholder in the template with an argument. These are
     *                             matched by position - the first {@code %s} gets {@code
     *     errorMessageArgs[0]} , etc. Unmatched arguments will be appended to the formatted message in square
     *                             braces. Unmatched placeholders will be left as-is.
     * @param errorMessageArgs
     *                             the arguments to be substituted into the message template. Arguments are converted to
     *                             strings using {@link String#valueOf(Object)}.
     * @return the non-null reference that was validated
     * @throws NullPointerException
     *                              if {@code reference} is null
     */
    public static <T> T checkNotNull(final T reference,
        @Nullable final String errorMessageTemplate,
        @Nullable final Object... errorMessageArgs) {
        if (reference == null) {
            // If either of these parameters is null, the right thing happens anyway
            throw new IllegalArgumentException(format(required(errorMessageTemplate), errorMessageArgs));
        }
        return reference;
    }

    /**
     * Assert that an object is not <code>null</code> .
     *
     * <pre class="code">
     * Assert.notNull(clazz, &quot;The class must not be null&quot;);
     * </pre>
     *
     * @param object
     *                the object to check
     * @param message
     *                the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is <code>null</code>
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    @Nonnull
    public static <T> T notNull(@Nullable final T object, @Nonnull final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Assert that an object is not <code>null</code> .
     *
     * <pre class="code">
     * Assert.notNull(clazz);
     * </pre>
     *
     * @param object
     *               the object to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is <code>null</code>
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    @Nonnull
    public static <T> T notNull(@Nullable final T object) {
        return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that the given String is not empty; that is, it must not be <code>null</code> and not the empty String.
     *
     * <pre class="code">
     * Assert.hasLength(name, &quot;Name must not be empty&quot;);
     * </pre>
     *
     * @param text
     *                the String to check
     * @param message
     *                the exception message to use if the assertion fails
     * @return a {@link java.lang.String} object.
     */
    public static String hasLength(final String text, final String message) {
        if (isEmpty(text)) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    /**
     * Assert that the given String is not empty; that is, it must not be <code>null</code> and not the empty String.
     *
     * <pre class="code">
     * Assert.hasLength(name);
     * </pre>
     *
     * @param text
     *             the String to check
     * @return a {@link java.lang.String} object.
     */
    public static String hasLength(final String text) {
        return hasLength(text,
            "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * <p>
     * checkHasText.
     * </p>
     *
     * @param text
     *                      a {@link java.lang.String} object.
     * @param parameterName
     *                      a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Nonnull
    public static String checkHasText(@Nullable final String text, @Nonnull final String parameterName) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(required(parameterName));
        }
        return text;
    }

    /**
     * Assert that the given String has valid text content; that is, it must not be <code>null</code> and must contain
     * at least one non-whitespace character.
     *
     * <pre class="code">
     * Assert.hasText(name, &quot;'name' must not be empty&quot;);
     * </pre>
     *
     * @param text
     *                the String to check
     * @param message
     *                the exception message to use if the assertion fails
     * @return a {@link java.lang.String} object.
     */
    public static String hasText(final String text, final String message) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    /**
     * Assert that the given String has valid text content; that is, it must not be <code>null</code> and must contain
     * at least one non-whitespace character.
     *
     * <pre class="code">
     * Assert.hasText(name, &quot;'name' must not be empty&quot;);
     * </pre>
     *
     * @param text
     *             the String to check
     * @return a {@link java.lang.String} object.
     */
    public static String hasText(final String text) {
        return hasText(text,
            "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * Assert that the given text does not contain the given substring.
     *
     * <pre class="code">
     * Assert.doesNotContain(name, &quot;rod&quot;, &quot;Name must not contain 'rod'&quot;);
     * </pre>
     *
     * @param textToSearch
     *                     the text to search
     * @param substring
     *                     the substring to find within the text
     * @param message
     *                     the exception message to use if the assertion fails
     */
    public static void doesNotContain(final String textToSearch, final String substring, final String message) {
        if (isNotEmpty(textToSearch) && isNotEmpty(substring) && textToSearch.indexOf(substring) != -1) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given text does not contain the given substring.
     *
     * <pre class="code">
     * Assert.doesNotContain(name, &quot;rod&quot;);
     * </pre>
     *
     * @param textToSearch
     *                     the text to search
     * @param substring
     *                     the substring to find within the text
     */
    public static void doesNotContain(final String textToSearch, final String substring) {
        doesNotContain(textToSearch,
            substring,
            "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

    /**
     * Assert that an array has elements; that is, it must not be <code>null</code> and must have at least one element.
     *
     * <pre class="code">
     * Assert.notEmpty(array, &quot;The array must have elements&quot;);
     * </pre>
     *
     * @param array
     *                the array to check
     * @param message
     *                the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the object array is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return an array of T objects.
     */
    public static <T> T[] notEmpty(final T[] array, final String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Assert that an array has elements; that is, it must not be <code>null</code> and must have at least one element.
     *
     * <pre class="code">
     * Assert.notEmpty(array);
     * </pre>
     *
     * @param array
     *              the array to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the object array is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return an array of T objects.
     */
    public static <T> T[] notEmpty(final T[] array) {
        return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that an array has no null elements. Note: Does not complain if the array is empty!
     *
     * <pre class="code">
     * Assert.noNullElements(array, &quot;The array must have non-null elements&quot;);
     * </pre>
     *
     * @param array
     *                the array to check
     * @param message
     *                the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the object array contains a <code>null</code> element
     * @param <T>
     *            a T object.
     * @return an array of T objects.
     */
    public static <T> T[] noNullElements(final T[] array, final String message) {
        if (array != null) {
            for (final T element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
        return array;
    }

    /**
     * Assert that an array has no null elements. Note: Does not complain if the array is empty!
     *
     * <pre class="code">
     * Assert.noNullElements(array);
     * </pre>
     *
     * @param array
     *              the array to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the object array contains a <code>null</code> element
     * @param <T>
     *            a T object.
     * @return an array of T objects.
     */
    public static <T> T[] noNullElements(final T[] array) {
        return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    /**
     * Assert that a collection has elements; that is, it must not be <code>null</code> and must have at least one
     * element.
     *
     * <pre class="code">
     * Assert.notEmpty(collection, &quot;Collection must have elements&quot;);
     * </pre>
     *
     * @param collection
     *                   the collection to check
     * @param message
     *                   the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the collection is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return a {@link java.util.Collection} object.
     */
    public static <T> Collection<T> notEmpty(final Collection<T> collection, final String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return collection;
    }

    /**
     * Assert that a collection has elements; that is, it must not be <code>null</code> and must have at least one
     * element.
     *
     * <pre class="code">
     * Assert.notEmpty(collection, &quot;Collection must have elements&quot;);
     * </pre>
     *
     * @param collection
     *                   the collection to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the collection is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return a {@link java.util.Collection} object.
     */
    public static <T> Collection<T> notEmpty(final Collection<T> collection) {
        return notEmpty(collection,
            "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that a list has elements; that is, it must not be <code>null</code> and must have at least one element.
     *
     * <pre class="code">
     * Assert.notEmpty(list, &quot;list must have elements&quot;);
     * </pre>
     *
     * @param collection
     *                   the list to check
     * @param message
     *                   the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the list is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return a {@link java.util.Collection} object.
     */
    public static <T> List<T> notEmpty(final List<T> list, final String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return list;
    }

    /**
     * Assert that a list has elements; that is, it must not be <code>null</code> and must have at least one element.
     *
     * <pre class="code">
     * Assert.notEmpty(list, &quot;list must have elements&quot;);
     * </pre>
     *
     * @param collection
     *                   the list to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the list is <code>null</code> or has no elements
     * @param <T>
     *            a T object.
     * @return a {@link java.util.Collection} object.
     */
    public static <T> List<T> notEmpty(final List<T> list) {
        return notEmpty(list, "[Assertion failed] - this list must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that a Map has entries; that is, it must not be <code>null</code> and must have at least one entry.
     *
     * <pre class="code">
     * Assert.notEmpty(map, &quot;Map must have entries&quot;);
     * </pre>
     *
     * @param map
     *                the map to check
     * @param message
     *                the exception message to use if the assertion fails
     * @throws java.lang.IllegalArgumentException
     *                                            if the map is <code>null</code> or has no entries
     * @param <T>
     *            a T object.
     * @param <R>
     *            a R object.
     * @return a {@link java.util.Map} object.
     */
    public static <T, R> Map<T, R> notEmpty(final Map<T, R> map, final String message) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return map;
    }

    /**
     * Assert that a Map has entries; that is, it must not be <code>null</code> and must have at least one entry.
     *
     * <pre class="code">
     * Assert.notEmpty(map);
     * </pre>
     *
     * @param map
     *            the map to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the map is <code>null</code> or has no entries
     * @param <T>
     *            a T object.
     * @param <R>
     *            a R object.
     * @return a {@link java.util.Map} object.
     */
    public static <T, R> Map<T, R> notEmpty(final Map<T, R> map) {
        return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     *
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param clazz
     *              the required class
     * @param obj
     *              the object to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static void isInstanceOf(final Class<?> clazz, final Object obj) {
        isInstanceOf(clazz, obj, "");
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     *
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param type
     *                the type to check against
     * @param obj
     *                the object to check
     * @param message
     *                a message which will be prepended to the message produced by the function itself, and which may be
     *                used to provide context. It should normally end in a ": " or ". " so that the function generate
     *                message looks ok when prepended to it.
     * @throws java.lang.IllegalArgumentException
     *                                            if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static void isInstanceOf(final Class<?> type, final Object obj, final String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(message + "Object of class ["
                    + (obj != null ? obj.getClass().getName() : "null") + "] must be an instance of " + type);
        }
    }

    /**
     * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
     *
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType
     *                  the super type to check
     * @param subType
     *                  the sub type to check
     * @throws java.lang.IllegalArgumentException
     *                                            if the classes are not assignable
     */
    public static void isAssignable(final Class<?> superType, final Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    /**
     * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
     *
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType
     *                  the super type to check against
     * @param subType
     *                  the sub type to check
     * @param message
     *                  a message which will be prepended to the message produced by the function itself, and which may
     *                  be used to provide context. It should normally end in a ": " or ". " so that the function
     *                  generate message looks ok when prepended to it.
     * @throws java.lang.IllegalArgumentException
     *                                            if the classes are not assignable
     */
    public static void isAssignable(final Class<?> superType, final Class<?> subType, final String message) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(message + subType + " is not assignable to " + superType);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalStateException</code> if the test result is <code>false</code>
     * . Call isTrue if you wish to throw IllegalArgumentException on an assertion failure.
     *
     * <pre class="code">
     * Assert.state(id == null, &quot;The id property must not already be initialized&quot;);
     * </pre>
     *
     * @param expression
     *                   a boolean expression
     * @param message
     *                   the exception message to use if the assertion fails
     * @throws java.lang.IllegalStateException
     *                                         if expression is <code>false</code>
     */
    public static void state(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalStateException</code> if the test result is <code>false</code>
     * . Call isTrue if you wish to throw IllegalArgumentException on an assertion failure.
     *
     * <pre class="code">
     * Assert.state(id == null, &quot;The {} property must not already be initialized&quot;, &quot;id&quot;);
     * </pre>
     *
     * @param expression
     *                         a boolean expression
     * @param message
     *                         the exception template message to use if the assertion fails
     * @param errorMessageArgs
     *                         the arguments to be substituted into the message template. Arguments are converted to
     *                         strings using {@link String#valueOf(Object)}.
     */
    public static void state(final boolean expression,
        final String message,
        @Nullable final Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(message, errorMessageArgs));
        }
    }

    /**
     * Assert a boolean expression, throwing {@link java.lang.IllegalStateException} if the test result is
     * <code>false</code>.
     * <p>
     * Call {@link #isTrue(boolean)} if you wish to throw {@link java.lang.IllegalArgumentException} on an assertion
     * failure.
     *
     * <pre class="code">
     * Assert.state(id == null);
     * </pre>
     *
     * @param expression
     *                   a boolean expression
     * @throws java.lang.IllegalStateException
     *                                         if the supplied expression is <code>false</code>
     */
    public static void state(final boolean expression) {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * Asserts that two objects are equal. If they are not an @{link IllegalArgumentException} is thrown.
     *
     * @param name
     *                 a {@link java.lang.String} object.
     * @param expected
     *                 a T object.
     * @param got
     *                 a T object.
     * @return a T object.
     * @throws java.lang.IllegalArgumentException
     *                                            if any.
     * @param <T>
     *            a T object.
     */
    public static <T> T equalsTo(final String name, final T expected, final T got) throws IllegalArgumentException {
        if (!expected.equals(got)) {
            throw new IllegalArgumentException(name + ". Expected:" + expected + " but got: " + got);
        }
        return got;
    }

    static String required(final String parameterName) {
        return format("{} parameter is required", parameterName);
    }

    /**
     * Substitutes each <code>{}</code> in {@code template} with an argument. These are matched by position: the first
     * {@code %s} gets {@code args[0]}, etc. If there are more arguments than placeholders, the unmatched arguments will
     * be appended to the end of the formatted message in square braces.
     *
     * @param template
     *                 a non-null string containing 0 or more {@code %s} placeholders.
     * @param args
     *                 the arguments to be substituted into the message template. Arguments are converted to strings
     *                 using {@link String#valueOf(Object)}. Arguments can be null.
     */
    @Nonnull
    static String format(@Nullable String template, @Nullable final Object... args) {
        template = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the '%s' placeholders
        final StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            final int placeholderStart = template.indexOf("{}", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }

    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * isEmpty(null)      = true
     * isEmpty("")        = true
     * isEmpty(" ")       = false
     * isEmpty("bob")     = false
     * isEmpty("  bob  ") = false
     * </pre>
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in
     * isBlank().
     * </p>
     *
     * @param str
     *            the String to check, may be null
     * @return {@code true} if the String is empty or {@code null}
     */
    private static boolean isEmpty(@Nullable final String str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     *
     * <pre>
     * isNotEmpty(null)      = false
     * isNotEmpty("")        = false
     * isNotEmpty(" ")       = true
     * isNotEmpty("bob")     = true
     * isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str
     *            the String to check, may be null
     * @return {@code true} if the String is not empty and not {@code null}
     */
    private static boolean isNotEmpty(@Nullable final String str) {
        return !isEmpty(str);
    }

    /**
     * <p>
     * Checks if a String is whitespace, empty ("") or null.
     * </p>
     *
     * <pre>
     * isBlank(null)      = true
     * isBlank("")        = true
     * isBlank(" ")       = true
     * isBlank("bob")     = false
     * isBlank("  bob  ") = false
     * </pre>
     *
     * @param str
     *            the String to check, may be null
     * @return {@code true} if the String is {@code null}, empty or whitespace
     */
    private static boolean isBlank(final String str) {
        int strLen;
        if (str == null) {
            return true;
        }
        strLen = str.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
