package com.pmi.tpd.spring.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class ClasspathI18nMessageSourceTest {

    @Test
    public void shouldResolveResources() throws IOException {
        final String[] baseNames = ClasspathI18nMessageSource.getResolveResource(false);
        assertEquals(2, baseNames.length);
        MatcherAssert.assertThat(baseNames,
            Matchers.arrayContainingInAnyOrder("i18n/foo-core/app-foo", "i18n/foo-core/app-bar"));
    }
}
