package com.pmi.tpd.spring.env;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PropertySourcesLoader}.
 */
public class PropertySourcesLoaderTest {

    private final PropertySourcesLoader loader = new PropertySourcesLoader();

    @Test
    public void fileExtensions() {
        assertTrue(this.loader.getAllFileExtensions().contains("yml"));
        assertTrue(this.loader.getAllFileExtensions().contains("yaml"));
        assertTrue(this.loader.getAllFileExtensions().contains("properties"));
        assertTrue(this.loader.getAllFileExtensions().contains("xml"));
    }

}
