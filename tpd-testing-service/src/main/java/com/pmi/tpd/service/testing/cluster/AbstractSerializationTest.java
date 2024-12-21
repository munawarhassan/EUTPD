package com.pmi.tpd.service.testing.cluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.ObjectDataInput;

public abstract class AbstractSerializationTest<T extends Serializable> {

    protected static InternalSerializationService serializationService;

    @BeforeAll
    public static void beforeClass() {
        serializationService = new DefaultSerializationServiceBuilder().build();
    }

    @AfterAll
    public static void afterClass() {
        serializationService = null;
    }

    @Test
    public void testSerialization() throws Exception {
        final Serializable original = newObject();
        final byte[] serialized = toBytes(original);
        final Serializable deserialized = (Serializable) fromBytes(serialized);
        MatcherAssert.assertThat(deserialized, new SameFieldValuesAs<Serializable>(original, ignoredFields()));
    }

    protected Set<String> ignoredFields() {
        return Collections.emptySet();
    }

    protected abstract T newObject();

    private static Object readField(final Field field, final Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not retrieve " + field + " from " + target, e);
        }
    }

    private static Set<Field> fieldFrom(Class<?> klass, final Set<String> ignoredFields) {
        final HashSet<Field> fields = Sets.newHashSet();

        while (klass != null) {
            for (final Field field : klass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || ignoredFields.contains(field.getName())) {
                    continue;
                }
                fields.add(field);
            }

            klass = klass.getSuperclass();
        }

        return fields;
    }

    private static <T> List<FieldMatcher> fieldMatchersFor(final T bean, final Set<Field> fields) {
        final List<FieldMatcher> result = Lists.newArrayListWithCapacity(fields.size());
        for (final Field field : fields) {
            result.add(new FieldMatcher(field, bean));
        }
        return result;
    }

    public static byte[] toBytes(final Object object) throws IOException {
        if (object == null) {
            return null;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectDataOutputStream objectOut = SerializationUtil.createObjectDataOutputStream(out,
            serializationService);
        objectOut.writeObject(object);
        objectOut.flush();
        return out.toByteArray();
    }

    public static <T> T fromBytes(final byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }

        final ObjectDataInput objectIn = serializationService.createObjectDataInput(bytes);
        return objectIn.readObject();
    }

    private static class FieldMatcher extends DiagnosingMatcher<Object> {

        private final Field field;

        private final Matcher<Object> matcher;

        public FieldMatcher(final Field field, final Object expectedObject) {
            this.field = field;
            this.matcher = IsEqual.equalTo(AbstractSerializationTest.readField(field, expectedObject));
        }

        @Override
        public boolean matches(final Object actual, final Description mismatch) {
            final Object actualValue = AbstractSerializationTest.readField(this.field, actual);
            if (!this.matcher.matches(actualValue)) {
                mismatch.appendText(this.field.getName() + " ");
                this.matcher.describeMismatch(actualValue, mismatch);
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText(this.field.getName() + ": ").appendDescriptionOf(this.matcher);
        }
    }

    private static class SameFieldValuesAs<T> extends TypeSafeDiagnosingMatcher<T> {

        private final T expectedBean;

        private final Set<Field> fields;

        private final List<AbstractSerializationTest.FieldMatcher> fieldMatchers;

        private final Set<String> ignoredFields;

        public SameFieldValuesAs(final T expectedBean, final Set<String> ignoredFields) {
            this.expectedBean = expectedBean;
            this.fields = AbstractSerializationTest.fieldFrom(expectedBean.getClass(), ignoredFields);
            this.fieldMatchers = AbstractSerializationTest.fieldMatchersFor(expectedBean, this.fields);
            this.ignoredFields = ignoredFields;
        }

        @Override
        public boolean matchesSafely(final T bean, final Description mismatch) {
            return isCompatibleType(bean, mismatch) && hasNoExtraFields(bean, mismatch)
                    && hasMatchingValues(bean, mismatch);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("same field values as " + this.expectedBean.getClass().getSimpleName())
                    .appendList(" [", ", ", "]", this.fieldMatchers);
        }

        private boolean isCompatibleType(final T item, final Description mismatchDescription) {
            if (!this.expectedBean.getClass().isAssignableFrom(item.getClass())) {
                mismatchDescription.appendText("is incompatible type: " + item.getClass().getSimpleName());
                return false;
            }
            return true;
        }

        private boolean hasNoExtraFields(final T item, final Description mismatchDescription) {
            final Set<Field> actualFields = AbstractSerializationTest.fieldFrom(item.getClass(), this.ignoredFields);
            actualFields.removeAll(this.fields);
            if (!actualFields.isEmpty()) {
                mismatchDescription.appendText("has extra fields called " + actualFields);
                return false;
            }
            return true;
        }

        private boolean hasMatchingValues(final T item, final Description mismatchDescription) {
            for (final AbstractSerializationTest.FieldMatcher fieldMatcher : this.fieldMatchers) {
                if (!fieldMatcher.matches(item)) {
                    fieldMatcher.describeMismatch(item, mismatchDescription);
                    return false;
                }
            }
            return true;
        }
    }
}
