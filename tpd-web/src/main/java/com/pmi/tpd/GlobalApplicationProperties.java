package com.pmi.tpd;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.Environment;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.IGlobalApplicationProperties;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Named
@JsonSerialize()
public class GlobalApplicationProperties implements IGlobalApplicationProperties {

    /** */
    private final IApplicationConfiguration applicationConfiguration;

    @Inject
    public GlobalApplicationProperties(@Nonnull final IApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = checkNotNull(applicationConfiguration, "applicationConfiguration");
        checkNotNull(applicationConfiguration.getBuildUtilsInfo(), "applicationConfiguration.getBuildUtilsInfo");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseUrl() {
        // TODO [devacfr] implement baseurl
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    /**
     * @return
     */
    public String getApplicationName() {
        return Product.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return Product.getFullName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return getBuildUtilsInfo().getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getBuildDate() {
        return getBuildUtilsInfo().getCurrentBuildDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildNumber() {
        return getBuildUtilsInfo().getCurrentBuildNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public Path getHomeDirectory() {
        return applicationConfiguration.getHomeDirectory();
    }

    /**
     * @return
     */
    public IBuildUtilsInfo getBuildUtilsInfo() {
        return applicationConfiguration.getBuildUtilsInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment getEnvironment() {
        return applicationConfiguration.getEnvironment();
    }
}
