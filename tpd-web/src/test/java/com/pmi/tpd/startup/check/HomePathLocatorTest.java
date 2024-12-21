package com.pmi.tpd.startup.check;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.web.testing.AbstractJunitTest;

public class HomePathLocatorTest extends AbstractJunitTest {

    @Test
    public void getHome() throws IOException {
        final MockEnvironment environment = new MockEnvironment();
        final HomePathLocator homePathLocator = new HomePathLocator(environment);
        assertNull(homePathLocator.getHome());
    }

    @Test
    public void getHomeWithDefaultValue() throws IOException {
        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY,
            PATH_HOME.getAbsolutePath());
        final HomePathLocator homePathLocator = new HomePathLocator(environment);
        assertEquals(PATH_HOME.getAbsolutePath(), homePathLocator.getHome());
    }

    @Test
    public void getDisplayName() throws IOException {
        final MockEnvironment environment = new MockEnvironment();
        final HomePathLocator homePathLocator = new HomePathLocator(environment);
        assertEquals("Application Properties", homePathLocator.getDisplayName());
    }
}
