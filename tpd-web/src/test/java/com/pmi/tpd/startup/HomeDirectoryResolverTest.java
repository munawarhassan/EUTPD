package com.pmi.tpd.startup;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HomeDirectoryResolverTest extends MockitoTestCase {

    private String oldHome;

    @Mock(lenient = true)
    private EnvironmentVariableResolver environmentVariableResolver;

    private HomeDirectoryResolver homeDirectoryResolver;

    @BeforeEach
    public void setUp() throws Exception {
        oldHome = System.getProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);

        homeDirectoryResolver = new HomeDirectoryResolver(environmentVariableResolver);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (oldHome == null) {
            System.clearProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);
        } else {
            System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, oldHome);
        }
    }

    @Test
    public void testResolveWithEnvironmentVariable() {
        clearProperties();

        when(environmentVariableResolver.getValue(ApplicationConstants.HOME_ENV_VARIABLE)).thenReturn("env/home");

        final HomeDirectoryDetails homeDetails = homeDirectoryResolver.resolve();

        final File actualHome = homeDetails.getHome();
        final File inputHome = new File("env/home");
        assertFalse(actualHome.equals(inputHome), "Resolved home path should be absolute");
        assertEquals(inputHome.getAbsoluteFile(), actualHome);
    }

    @Test
    public void testHomeResolveWithEnvironmentVariableWithTilde() {
        assertThrows(IllegalStateException.class, () -> {
            clearProperties();

            when(environmentVariableResolver.getValue(HomeDirectoryResolver.HOME_DIR_ENV_VARIABLE))
                    .thenReturn("~/env/home");
            homeDirectoryResolver.resolve();
        });
    }

    @Test
    public void testResolveWithSystemProperty() {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "prop/home");
        when(environmentVariableResolver.getValue(HomeDirectoryResolver.HOME_DIR_ENV_VARIABLE)).thenReturn("env/home");

        // system property should take precedence if both are declared
        final HomeDirectoryDetails homeDetails = homeDirectoryResolver.resolve();

        final File actualHome = homeDetails.getHome();
        final File inputHome = new File("prop/home");
        assertFalse(actualHome.equals(inputHome), "Resolved home path should be absolute");
        assertEquals(inputHome.getAbsoluteFile(), actualHome);

    }

    @Test
    public void testHomeResolveWithSystemPropertyContainingTilde() {
        assertThrows(IllegalStateException.class, () -> {
            System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "~/prop/home");

            homeDirectoryResolver.resolve();
        });
    }

    @Test
    public void testResolveWithoutSystemPropertyOrEnvironmentVariable() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            clearProperties();

            when(environmentVariableResolver.getValue(HomeDirectoryResolver.HOME_DIR_ENV_VARIABLE)).thenReturn(null);
            homeDirectoryResolver.resolve();
        });
    }

    @Test
    public void testResolveDifferent() throws Exception {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "env/home");

        final HomeDirectoryDetails homeDetails = homeDirectoryResolver.resolve();

        final File actualHome = homeDetails.getHome();
        final File inputHome = new File("env/home");
        assertFalse(actualHome.equals(inputHome), "Resolved home path should be absolute");
        assertEquals(inputHome.getAbsoluteFile(), actualHome);

    }

    private void clearProperties() {
        System.clearProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);
    }
}
