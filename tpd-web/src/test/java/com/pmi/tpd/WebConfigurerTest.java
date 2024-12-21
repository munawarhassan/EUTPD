package com.pmi.tpd;

import javax.servlet.ServletException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.spring.context.ConfigFileLoader;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class WebConfigurerTest extends MockitoTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        System.clearProperty(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY);
        System.clearProperty(ApplicationConstants.PROFILES_ENV_VARIABLE);
    }

    @Test
    public void shouldUseProductionProfileByDefault() throws ServletException {
        final ConfigurableEnvironment environment = new StandardServletEnvironment();
        WebAppInitializer.initializeActiveProfiles(environment);

        assertThat(environment.getActiveProfiles(), Matchers.arrayContaining("production"));
    }

    /**
     * Should use Spring Profile. Used in Service shell script.
     *
     * @throws ServletException
     * @see TPD-263
     */
    @Test
    public void shouldUseSpringProfile() throws ServletException {
        System.setProperty(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY, "dev");
        final ConfigurableEnvironment environment = new StandardServletEnvironment();
        WebAppInitializer.initializeActiveProfiles(environment);

        assertThat(environment.getActiveProfiles(), Matchers.arrayContaining("dev"));
    }

    @Test
    public void shouldUseAppProfileInsteadSpringProfile() throws ServletException {
        System.setProperty(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY, "dev");
        System.setProperty(ApplicationConstants.PROFILES_ENV_VARIABLE, "qa");
        final ConfigurableEnvironment environment = new StandardServletEnvironment();
        WebAppInitializer.initializeActiveProfiles(environment);

        assertThat(environment.getActiveProfiles(), Matchers.arrayContaining("qa"));
    }
}
