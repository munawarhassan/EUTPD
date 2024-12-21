package com.pmi.tpd.spring.context;

import java.io.StringReader;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class PlaceholderResolvingPropertiesTest extends TestCase {

    @Test
    public void testGetProperty() throws Exception {
        final PlaceholderResolvingProperties properties = load("example.value=5\nexample.resolving=${example.value}");
        assertEquals("5", properties.getProperty("example.resolving"));
    }

    @Test
    public void testGetPropertyWithFirstLevelCycle() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            final PlaceholderResolvingProperties properties = load("example.value=${example.value}");
            properties.getProperty("example.value");
        });
    }

    @Test
    public void testGetPropertyWithSecondLevelCycle() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            final PlaceholderResolvingProperties properties = load(
                "example.value=${example.resolving}\n" + "example.resolving=${example.value}");
            properties.getProperty("example.value");
        });
    }

    @Test
    public void testGetPropertyIgnoresUnresolvedPlaceholders() throws Exception {
        final PlaceholderResolvingProperties properties = load("example.resolving=${example.value}");
        assertEquals("${example.value}", properties.getProperty("example.resolving"));
    }

    private static PlaceholderResolvingProperties load(final String value) throws Exception {
        final Properties properties = new Properties();
        properties.load(new StringReader(value));

        return new PlaceholderResolvingProperties(properties);
    }
}
