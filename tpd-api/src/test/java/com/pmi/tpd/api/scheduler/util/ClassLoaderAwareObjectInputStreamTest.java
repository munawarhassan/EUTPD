package com.pmi.tpd.api.scheduler.util;

import static com.pmi.tpd.api.scheduler.Constants.BYTES_DEADF00D;
import static com.pmi.tpd.api.scheduler.Constants.BYTES_EMPTY_MAP;
import static com.pmi.tpd.api.scheduler.Constants.BYTES_NULL;
import static com.pmi.tpd.api.scheduler.Constants.BYTES_PARAMETERS;
import static com.pmi.tpd.api.scheduler.Constants.EMPTY_MAP;
import static com.pmi.tpd.api.scheduler.Constants.PARAMETERS;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.common.base.Throwables;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings("ConstantConditions")
public class ClassLoaderAwareObjectInputStreamTest extends TestCase {

    @Test
    public void testNulls() throws Exception {
        assertConstructorThrows(IllegalArgumentException.class, null, null);
        assertConstructorThrows(IllegalArgumentException.class, null, BYTES_PARAMETERS);
        assertConstructorThrows(IllegalArgumentException.class, getClass().getClassLoader(), null);
    }

    @Test
    public void testInvalidSerializationData() {
        assertConstructorThrows(IOException.class, getClass().getClassLoader(), BYTES_DEADF00D);
    }

    @Test
    public void testNullMap() throws IOException, ClassNotFoundException {
        assertReadObject(null, getClass().getClassLoader(), BYTES_NULL);
    }

    @Test
    public void testEmptyMap() throws IOException, ClassNotFoundException {
        assertReadObject(EMPTY_MAP, getClass().getClassLoader(), BYTES_EMPTY_MAP);
    }

    @Test
    public void testParameters() throws IOException, ClassNotFoundException {
        assertReadObject(PARAMETERS, getClass().getClassLoader(), BYTES_PARAMETERS);
    }

    @Test
    public void testResolveClassYieldsFirstExceptionWhenNotFound() throws Exception {
        // The system classloader isn't going to find "foo.bar" either, and *our*
        // exception is the one we want
        final ClassNotFoundException cnfe = new ClassNotFoundException("Expected");
        assertResolveClassThrows(cnfe, classLoaderThatThrows(cnfe), "foo.bar");
    }

    @Test
    public void testResolveClassSucceedsUsingOurClassLoader() throws Exception {
        final TestStringClassLoader classLoader = new TestStringClassLoader();
        assertResolveClass(String.class, classLoader, String.class.getName());

        // Make sure it came from us, since the system classloader would have succeeded
        // too
        MatcherAssert.assertThat(1, Matchers.is(classLoader.getCount()));

    }

    @Test
    public void testResolveClassSucceedsByFallbackForNormalClasses() throws Exception {
        // The system classloader with find java.lang.String
        final ClassNotFoundException cnfe = new ClassNotFoundException("Expected");
        assertResolveClass(String.class, classLoaderThatThrows(cnfe), String.class.getName());
    }

    @Test
    public void testResolveClassSucceedsByFallbackForPrimitives() throws Exception {
        // A special case that ObjectInputStream supports...
        final ClassNotFoundException cnfe = new ClassNotFoundException("Expected");
        assertResolveClass(Integer.TYPE, classLoaderThatThrows(cnfe), "int");
    }

    @Test
    public void testResolveClassDoesNotAttemptFallbackOnSerializationErrors() throws Exception {
        // The system classloader would find java.lang.String, but since we throw
        // IOException it does not try
        final IOException ioe = new IOException("Expected");
        assertResolveClassThrows(ioe, classLoaderThatThrows(ioe), String.class.getName());
    }

    static ClassLoader classLoaderThatThrows(final Throwable e) {
        return new TestExceptionClassLoader(e);
    }

    static ObjectStreamClass desc(final String className) {
        final ObjectStreamClass osc = mock(ObjectStreamClass.class);
        when(osc.getName()).thenReturn(className);
        return osc;
    }

    static void assertConstructorThrows(final Class<? extends Exception> expected,
        final ClassLoader classLoader,
        final byte[] bytes) {
        try {
            final ClassLoaderAwareObjectInputStream is = new ClassLoaderAwareObjectInputStream(classLoader, bytes);
            is.close();
            try {
                fail("Expected construction to fail with " + expected.getName() + ", but it succeeded!");
            } finally {
                is.close();
            }
        } catch (final Exception ex) {
            MatcherAssert.assertThat(ex, instanceOf(expected));
        }
    }

    private static void assertReadObject(final Map<String, Serializable> expected,
        final ClassLoader classLoader,
        final byte[] bytes) throws IOException, ClassNotFoundException {
        final ClassLoaderAwareObjectInputStream is = new ClassLoaderAwareObjectInputStream(classLoader, bytes);
        try {
            assertEquals(expected, is.readObject());
        } finally {
            is.close();
        }
    }

    private static void assertResolveClass(final Class<?> expected,
        final ClassLoader classLoader,
        final String className) throws IOException, ClassNotFoundException {
        final ClassLoaderAwareObjectInputStream is = new ClassLoaderAwareObjectInputStream(classLoader,
                BYTES_EMPTY_MAP);
        try {
            assertEquals(expected, is.resolveClass(desc(className)));
        } finally {
            is.close();
        }
    }

    private static void assertResolveClassThrows(final Exception expected,
        final ClassLoader classLoader,
        final String className) throws IOException, ClassNotFoundException {
        final ClassLoaderAwareObjectInputStream is = new ClassLoaderAwareObjectInputStream(classLoader,
                BYTES_EMPTY_MAP);
        try {
            throw new AssertionError("Expected resolveClass to throw " + expected + ", but got "
                    + is.resolveClass(desc(className)).getName());
        } catch (final Exception ex) {
            assertSame(expected, Throwables.getRootCause(ex));
        } finally {
            is.close();
        }
    }

    private static class TestExceptionClassLoader extends ClassLoader {

        private final Throwable e;

        public TestExceptionClassLoader(final Throwable e) {
            this.e = e;
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            Throwables.throwIfInstanceOf(e, ClassNotFoundException.class);
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

    }

    private static class TestStringClassLoader extends ClassLoader {

        private int count = 0;

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            count++;
            return String.class;
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            return loadClass(name, true);
        }

        public int getCount() {
            return count;
        }
    }
}
