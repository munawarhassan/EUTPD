package com.pmi.tpd.spring.env;

import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link PropertiesPropertySourceLoader}.
 */
public class PropertiesPropertySourceLoaderTest extends TestCase {

    private final PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();

    static {
        // open this namespace to spring-core
        final Module springCoreModule = PropertySource.class.getModule();
        final Class<?> cl = PropertiesPropertySourceLoaderTest.class;
        if (!cl.getModule().isOpen(cl.getPackageName(), springCoreModule)) {
            cl.getModule().addOpens(cl.getPackageName(), springCoreModule);
        }
    }

    @Test
    public void getFileExtensions() throws Exception {
        MatcherAssert.assertThat(this.loader.getFileExtensions(), equalTo(new String[] { "properties", "xml" }));
    }

    @Test
    public void loadProperties() throws Exception {

        final PropertySource<?> source = this.loader
                .load("test.properties", new ClassPathResource("test-properties.properties", this.getClass()), null);
        MatcherAssert.assertThat(source.getProperty("test"), equalTo((Object) "properties"));
    }

    @Test
    public void loadXml() throws Exception {
        final PropertySource<?> source = this.loader
                .load("test.xml", new ClassPathResource("test-xml.xml", getClass()), null);
        MatcherAssert.assertThat(source.getProperty("test"), equalTo((Object) "xml"));
    }

}
