package com.pmi.tpd.testing.junit5;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.TestAbortedException;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

/**
 * This class allow to migrate form JUnit 4.x syntax to JUnit 5.
 *
 * @since 1.0
 */
public abstract class TestCase extends Assertions {

    public TestInfo testInfo;

    /**
     * Creates new instance.
     */
    public TestCase() {

    }

    /**
     * Gets the file system path representation of this test class.
     *
     * @return Returns {@code String} representing the file system location path of this test class.
     */
    public final String getPackagePath() {
        return getPackagePath(this.getClass());
    }

    /**
     * Gets the file system path representation of this test class.
     *
     * @param testClass
     *            test class to use.
     * @return Returns {@code String} representing the file system location path of this test class.
     */
    public static final String getPackagePath(final Class<?> testClass) {
        return testClass.getPackage().getName().replace('.', '/');
    }

    /**
     * Gets the resource path location according to the {@code testClass} package
     *
     * @param testClass
     *            a test class to use.
     * @param resource
     *            a name of resource
     * @return Returns a {@link InputStream} representing the resource path location.
     * @since 1.3
     */
    public static final InputStream getResourceAsStream(final Class<?> testClass, final String resource) {
        InputStream in = testClass.getResourceAsStream(resource);
        final String path = testClass.getPackageName().replace('.', '/') + '/' + resource;
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(path);
        }
        // find in module if test executed in named module. Use mainly for Eclipse IDE
        if (in == null && testClass.getModule() != null) {
            try {
                in = testClass.getModule().getResourceAsStream(path);
            } catch (final IOException e) {
            }
        }
        return in;
    }

    /**
     * Gets the resource path location according to the {@code testClass} package
     *
     * @param testClass
     *            a test class to use.
     * @param resource
     *            a name of resource
     * @return Returns a {@link String} representing the resource path location.
     * @since 1.3
     */
    public final InputStream getResourceAsStream(final String resource) {
        return getResourceAsStream(getClass(), resource);
    }

    /**
     * Gets the resource path location according to the {@code testClass} package
     *
     * @param testClass
     *            a test class to use.
     * @param resource
     *            a name of resource
     * @return Returns a {@link String} representing the resource path location.
     * @since 1.3
     */
    public static final URL getResource(final Class<?> testClass, final String resource) {
        URL url = testClass.getResource(resource);
        final String path = testClass.getPackageName().replace('.', '/') + '/' + resource;
        if (url == null) {
            url = ClassLoader.getSystemResource(path);
        }
        return url;
    }

    /**
     * Gets the resource path location according to the {@code testClass} package
     *
     * @param testClass
     *            a test class to use.
     * @param resource
     *            a name of resource
     * @return Returns a {@link String} representing the resource path location.
     * @since 1.3
     */
    public final URL getResource(final String resource) {
        return getResource(getClass(), resource);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method is called before a test is executed (for
     * compatibility).
     *
     * @throws Exception
     *             exception to raise.
     */
    @BeforeEach
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void beforeEach(final TestInfo testInfo) throws Exception {
        this.testInfo = testInfo;
    }

    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed
     * (for compatibility).
     *
     * @throws Exception
     *             exception to raise.
     */
    @AfterEach
    public void afterEach() throws Exception {

    }

    public void assertEqualsAndHashcode(final Object expected, final Object actual) {
        assertEquals(expected, actual);
        assertEquals(expected.hashCode(), actual.hashCode());
    }

    public void assertNotEqualsAndHashcode(final Object expected, final Object actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(expected.hashCode(), actual.hashCode());
    }

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by <code>matcher</code>. If not, an
     * {@link AssertionError} is thrown with information about the matcher and failing value. Example:
     *
     * <pre>
     *   assertThat(0, is(1)); // fails:
     *     // failure message:
     *     // expected: is &lt;1&gt;
     *     // got value: &lt;0&gt;
     *   assertThat(0, is(not(1))) // passes
     * </pre>
     *
     * <code>org.hamcrest.Matcher</code> does not currently document the meaning of its type parameter <code>T</code>.
     * This method assumes that a matcher typed as <code>Matcher&lt;T&gt;</code> can be meaningfully applied only to
     * values that could be assigned to a variable of type <code>T</code>.
     *
     * @param <T>
     *            the static type accepted by the matcher (this can flag obvious compile-time problems such as
     *            {@code assertThat(1, is("a"))}
     * @param actual
     *            the computed value being compared
     * @param matcher
     *            an expression, built of {@link Matcher}s, specifying allowed values
     * @see org.hamcrest.CoreMatchers
     */
    public static <T> void assertThat(final T actual, final Matcher<? super T> matcher) {
        MatcherAssert.assertThat("", actual, matcher);
    }

    public static <T> void assertThat(final String reason, final T actual, final Matcher<? super T> matcher) {
        MatcherAssert.assertThat(reason, actual, matcher);
    }

    /**
     * Asserts that {@code text} contains all {@code substrings} parameters. If they are not, an {@link AssertionError}
     * is thrown.
     *
     * @param text
     *            the text to assert.
     * @param substrings
     *            list of string must be contained in text.
     */
    public static void assertContains(final String text, final String... substrings) {
        int startingFrom = 0;
        for (final String substring : substrings) {
            final int index = text.indexOf(substring, startingFrom);
            assertTrue(index >= startingFrom,
                String.format("Expected \"%s\" to contain substring \"%s\"", text, substring));
            startingFrom = index + substring.length();
        }

        final String lastSubstring = substrings[substrings.length - 1];
        assertTrue(text.indexOf(lastSubstring, startingFrom) == -1,
            String.format("Expected \"%s\" to contain substring \"%s\" only once),", text, lastSubstring));
    }

    /**
     * Assume a precondition holds for a certain value. Does nothing more if the precondition holds; otherwise throws an
     * exception which, when used in a test, causes the test to be marked as skipped.
     *
     * @param actual
     *            a value
     * @param matcher
     *            the precondition to be matched over the given value
     * @throws TestAbortedException
     *             if the precondition does not hold
     */
    public static <T> void assumeThat(final T actual, final Matcher<? super T> matcher) {
        assumeThat("", actual, matcher);
    }

    /**
     * Assume a precondition holds for a certain value. Does nothing more if the precondition holds; otherwise throws an
     * exception which, when used in a test, causes the test to be marked as skipped.
     *
     * @param reason
     *            a more specific description of the precondition
     * @param actual
     *            a value
     * @param matcher
     *            the precondition to be matched over the given value
     * @throws TestAbortedException
     *             if the precondition does not hold
     */
    public static <T> void assumeThat(final String reason, final T actual, final Matcher<? super T> matcher) {
        if (!matcher.matches(actual)) {
            final Description description = new StringDescription();
            description.appendText(reason);
            description.appendText("\nExpected: ");
            description.appendDescriptionOf(matcher);
            description.appendText("\n     but: ");
            matcher.describeMismatch(actual, description);
            throw new TestAbortedException(description.toString());
        }
    }

    public static <T extends Throwable> T assertThrowsWithMessage(final Class<T> expectedType,
        final Executable executable,
        final String message) {
        final T ex = assertThrows(expectedType, executable);
        assertEquals(message, ex.getMessage());
        return ex;
    }

    /**
     * @param location
     * @param actual
     * @throws IOException
     */
    public void approve(final String actual) throws IOException {
        verify(this.getClass(), getMethodName(), actual);
    }

    public void approve(final String suffix, final String actual) throws IOException {
        verify(this.getClass(), getMethodName() + '.' + suffix, actual);
    }

    public String getMethodName() {
        return testInfo.getTestMethod().get().getName();
    }

    /**
     * @param location
     * @param testClass
     * @param testName
     * @param actual
     * @throws IOException
     */
    private static void verify(final Class<?> testClass, final String testName, String actual) throws IOException {
        final String fileName = String.format("%s.%s.approved.txt", testClass.getSimpleName(), testName);
        final ByteSource byteSource = new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return getResourceAsStream(testClass, fileName);
            }
        };
        String expected = byteSource.asCharSource(Charsets.UTF_8).read();
        actual = actual.replaceAll("\r", "");
        expected = expected.replaceAll("\r", "");
        assertEquals(expected, actual);
    }

}
