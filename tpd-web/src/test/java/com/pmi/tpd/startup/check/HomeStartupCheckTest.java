package com.pmi.tpd.startup.check;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.core.context.GlobalApplicationConfiguration;
import com.pmi.tpd.startup.VersionHelper;
import com.pmi.tpd.web.testing.AbstractJunitTest;

public class HomeStartupCheckTest extends AbstractJunitTest {

    private HomeStartupCheck homeStartupCheck;

    private Environment environment;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        environment = new MockEnvironment();
        final GlobalApplicationConfiguration applicationConfiguration = new GlobalApplicationConfiguration(
                VersionHelper.builInfoOk(), environment);
        homeStartupCheck = new HomeStartupCheck(applicationConfiguration, environment);
    }

    @Test
    public void getProductionCheck() {
        assertEquals(homeStartupCheck, HomeStartupCheck.getProductionCheck());
    }

    @Test
    public void NonAbsoluteHomePath() throws Exception {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "target/path");
        assertEquals(true, homeStartupCheck.isOk());
        assertEquals(true, homeStartupCheck.isInitialised());
    }

    @Test
    public void testHomePathContainingTilde() {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "~/target/path");
        assertEquals(false, homeStartupCheck.isOk());
        assertEquals(true, homeStartupCheck.isInitialised());
        assertEquals(
            "The home directory [~/target/path] is invalid; tilde expansion is not supported. "
                    + "Please use an absolute path referring to a specific home directory.",
            homeStartupCheck.getFaultDescription());

    }

    @Test
    @Disabled
    // TODO correct it
    public void EmptyHomePath() throws Exception {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, "");
        assertEquals(false, homeStartupCheck.isOk());
        assertEquals(true, homeStartupCheck.isInitialised());
        assertEquals(
            "No app.home is configured.\n"
                    + "See conf/config-application.properties for instructions on setting app.home",
            homeStartupCheck.getFaultDescription());
        assertEquals(
            "No app.home is configured.\n"
                    + "See conf/config-application.properties for instructions on setting app.home",
            homeStartupCheck.getHtmlFaultDescription());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("Initial Check", homeStartupCheck.getName());
    }

    @Test
    public void getHome() throws Exception {
        homeStartupCheck.isOk();
        assertEquals(AbstractJunitTest.PATH_HOME, homeStartupCheck.getHome());
    }
}
