package com.pmi.tpd.spring.env.yaml;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.pmi.tpd.spring.env.YamlProcessor.MatchStatus;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link ArrayDocumentMatcher}.
 */
public class ArrayDocumentMatcherTest extends TestCase {

    @Test
    public void testMatchesSingleValue() throws IOException {
        final ArrayDocumentMatcher matcher = new ArrayDocumentMatcher("foo", "bar");
        assertEquals(MatchStatus.FOUND, matcher.matches(getProperties("foo: bar")));
    }

    @Test
    public void testDoesNotMatchesIndexedArray() throws IOException {
        final ArrayDocumentMatcher matcher = new ArrayDocumentMatcher("foo", "bar");
        assertEquals(MatchStatus.ABSTAIN, matcher.matches(getProperties("foo[0]: bar\nfoo[1]: spam")));
    }

    @Test
    public void testMatchesCommaSeparatedArray() throws IOException {
        final ArrayDocumentMatcher matcher = new ArrayDocumentMatcher("foo", "bar");
        assertEquals(MatchStatus.FOUND, matcher.matches(getProperties("foo: bar,spam")));
    }

    private Properties getProperties(final String values) throws IOException {
        return PropertiesLoaderUtils.loadProperties(new ByteArrayResource(values.getBytes()));
    }

}
