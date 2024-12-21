package com.pmi.tpd.api.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.RandomAccess;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;
import com.pmi.tpd.testing.junit5.TestCase;

public class ClassUtilsTest extends TestCase {

    @Test
    public void findAllTypes() {
        assertEquals(
            Sets.<Class<?>> newHashSet(List.class,
                AbstractList.class,
                Cloneable.class,
                RandomAccess.class,
                AbstractCollection.class,
                Iterable.class,
                Collection.class,
                ArrayList.class,
                Object.class,
                Serializable.class),
            ClassUtils.findAllTypes(ArrayList.class));
    }

    public void testGetTypeArguments() {
        assertEquals(asList(String.class), ClassUtils.getTypeArguments(BaseClass.class, Child.class));

        assertEquals(asList(String.class), ClassUtils.getTypeArguments(BaseClass.class, Baby.class));

        assertEquals(singletonList(null), ClassUtils.getTypeArguments(BaseClass.class, ForgotType.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getTypeArgumentsDifferentClassloader() throws Exception {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            final URL log4jUrl = getClass().getClassLoader().getResource("logback-test.xml");
            final URL root = new URL(log4jUrl.toExternalForm() + "/../");

            try (URLClassLoader urlCl = new URLClassLoader(new URL[] { root },
                    new FilteredClassLoader(MySuperClass.class))) {
                final ClosableClassLoader wrapCl = new ClosableClassLoader(getClass().getClassLoader());

                Thread.currentThread().setContextClassLoader(null);
                final Class<?> module = ClassUtils
                        .getTypeArguments((Class<Object>) urlCl.loadClass(MySuperClass.class.getName()),
                            (Class<? extends MySuperClass>) urlCl.loadClass(MySubClass.class.getName()))
                        .get(0);
                assertEquals(MyModule.class.getName(), module.getName());

                try (URLClassLoader urlCl2 = new URLClassLoader(new URL[] { root },
                        new FilteredClassLoader(MySuperClass.class))) {
                    assertTrue(wrapCl.loadClass(MySuperClass.class.getName()) == urlCl2
                            .loadClass(MySuperClass.class.getName()));
                    assertTrue(
                        wrapCl.loadClass(MySubClass.class.getName()) != urlCl2.loadClass(MySubClass.class.getName()));
                    assertTrue(wrapCl.loadClass(MySubClass.class.getName())
                            .getSuperclass() == urlCl2.loadClass(MySubClass.class.getName()).getSuperclass());

                    wrapCl.setClosed(true);
                    // Thread.currentThread().setContextClassLoader(urlCl2);
                    final Class<?> module2 = ClassUtils
                            .getTypeArguments((Class<Object>) urlCl2.loadClass(MySuperClass.class.getName()),
                                (Class<? extends MySuperClass>) urlCl2.loadClass(MySubClass.class.getName()))
                            .get(0);
                    assertEquals(MyModule.class.getName(), module2.getName());
                    assertTrue(module != module2);
                    assertTrue(module != MyModule.class);
                    assertTrue(module2 != MyModule.class);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getTypeArgumentsChildNotSubclass() {
        final Class fakeChild = BaseClass.class;
        try {
            assertEquals(singletonList(null), ClassUtils.getTypeArguments(Baby.class, fakeChild));
            fail("Should have failed");
        } catch (final IllegalArgumentException ex) {
            // this is good
        }
    }

    private static class BaseClass<T> {
    }

    private static class Child extends BaseClass<String> {
    }

    @SuppressWarnings("rawtypes")
    private static class ForgotType extends BaseClass {
    }

    private static class Mom<T> extends BaseClass<T> {
    }

    private static class Baby extends Mom<String> {
    }

    private static class ClosableClassLoader extends ClassLoader {

        private final ClassLoader delegate;

        private volatile boolean closed;

        public ClosableClassLoader(final ClassLoader delegate) {
            super(null);
            this.delegate = delegate;
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            checkClosed();
            return delegate.loadClass(name);
        }

        @Override
        public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            checkClosed();
            return delegate.loadClass(name);
        }

        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Closed");
            }
        }

        @Override
        public URL getResource(final String name) {
            checkClosed();
            return delegate.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(final String name) throws IOException {
            checkClosed();
            return delegate.getResources(name);
        }

        public void setClosed(final boolean closed) {
            this.closed = closed;
        }
    }

    private static class FilteredClassLoader extends ClassLoader {

        private final Collection<Class<?>> classes;

        public FilteredClassLoader(final Class<?>... classes) {
            super(null);
            this.classes = asList(classes);
        }

        @Override
        public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            for (final Class<?> cls : classes) {
                if (cls.getName().equals(name)) {
                    return cls;
                }
            }
            if (name.startsWith("java.")) {
                return ClassLoader.getSystemClassLoader().loadClass(name);
            }
            throw new ClassNotFoundException(name);
        }
    }

    public static class MySuperClass<T> {
    }

    public static class MySubClass extends MySuperClass<MyModule> {
    }

    public static class MyModule {
    }

}
