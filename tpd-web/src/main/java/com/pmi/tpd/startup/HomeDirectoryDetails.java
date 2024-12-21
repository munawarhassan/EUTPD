package com.pmi.tpd.startup;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class HomeDirectoryDetails {

    /** */
    private final File home;

    /** */
    private final File sharedHome;

    public HomeDirectoryDetails(@Nonnull final File home, @Nonnull final File sharedHome) {
        this.home = checkNotNull(home, "home");
        this.sharedHome = checkNotNull(sharedHome, "sharedHome");
    }

    @Nonnull
    public File getHome() {
        return home;
    }

    /**
     * @return
     * @since 1.3
     */
    @Nonnull
    public File getSharedHome() {
        return sharedHome;
    }

}
