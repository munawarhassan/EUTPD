package com.pmi.tpd.spring.env;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import com.pmi.tpd.spring.context.KeyValueList;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

/**
 * Tests for {@link YamlPropertySourceLoader}.
 */
public class YamlPropertySourceLoaderTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    public void load() throws Exception {
        final ByteArrayResource resource = new ByteArrayResource("foo:\n  bar: spam".getBytes());
        final PropertySource<?> source = this.loader.load("resource", resource, null);
        assertNotNull(source);
        assertEquals("spam", source.getProperty("foo.bar"));
    }

    @Test
    public void orderedItems() throws Exception {
        final StringBuilder yaml = new StringBuilder();
        final List<String> expected = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            yaml.append(c + ": value" + c + "\n");
            expected.add(String.valueOf(c));
        }
        final ByteArrayResource resource = new ByteArrayResource(yaml.toString().getBytes());
        final EnumerablePropertySource<?> source = (EnumerablePropertySource<?>) this.loader
                .load("resource", resource, null);
        assertNotNull(source);
        MatcherAssert.assertThat(source.getPropertyNames(), equalTo(expected.toArray(new String[] {})));
    }

    @Test
    public void mergeItems() throws Exception {
        final StringBuilder yaml = new StringBuilder();
        yaml.append("foo:\n  bar: spam\n");
        yaml.append("---\n");
        yaml.append("foo:\n  baz: wham\n");
        final ByteArrayResource resource = new ByteArrayResource(yaml.toString().getBytes());
        final PropertySource<?> source = this.loader.load("resource", resource, null);
        assertNotNull(source);
        assertEquals("spam", source.getProperty("foo.bar"));
        assertEquals("wham", source.getProperty("foo.baz"));
    }

    @Test
    public void timestampLikeItemsDoNotBecomeDates() throws Exception {
        final ByteArrayResource resource = new ByteArrayResource("foo: 2015-01-28".getBytes());
        final PropertySource<?> source = this.loader.load("resource", resource, null);
        assertNotNull(source);
        assertEquals("2015-01-28", source.getProperty("foo"));
    }

    @Test
    public void listItems() throws Exception {
        if (true) {
            final StringBuilder yaml = new StringBuilder();
            yaml.append("my:\n" //
                    + "    !!" + KeyValueList.class.getName() + "\n" //
                    + "    list:\n"//
                    + "     - key: key1\n" //
                    + "       value: value1\n" //
                    + "     - key: key2\n" //
                    + "       value: value2\n"//
                    + "property: propertyName\n");
            final ByteArrayResource resource = new ByteArrayResource(yaml.toString().getBytes());
            final PropertySource<?> source = this.loader.load("resource", resource, null);
            assertNotNull(source);
            final Object v = source.getProperty("my");
            assertEquals(KeyValueList.class, v.getClass());
            final KeyValueList list = (KeyValueList) v;
            assertEquals(2, list.getList().size());
        }
    }

}
