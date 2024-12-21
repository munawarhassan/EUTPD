package com.pmi.tpd.startup.check;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.core.env.Environment;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.core.startup.IHomePathLocator;

/**
 * Home path locator that gets the value from the application.properties file and not the database.
 */
class HomePathLocator implements IHomePathLocator {

    private final Environment environment;

    @Inject
    public HomePathLocator(@Nonnull final Environment environment) {
        this.environment = Assert.checkNotNull(environment, "environment");
    }

    @Override
    public String getHome() {
        return environment.getProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);
    }

    @Override
    public String getDisplayName() {
        return "Application Properties";
    }
}
