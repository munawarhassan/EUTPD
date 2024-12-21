package com.pmi.tpd.core.versioning.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.versioning.BuildVersion;

/**
 * Simple implementation of {@link com.pmi.tpd.core.versioning.BuildVersion}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
public final class BuildVersionImpl implements BuildVersion {

    /** build number. */
    private final String buildNumber;

    /** version. */
    private final String version;

    /**
     * <p>
     * Constructor for BuildVersionImpl.
     * </p>
     *
     * @param buildNumber
     *            a {@link java.lang.String} object.
     * @param version
     *            a {@link java.lang.String} object.
     */
    public BuildVersionImpl(@Nonnull final String buildNumber, @Nonnull final String version) {
        this.buildNumber = Assert.notNull(buildNumber);
        this.version = Assert.notNull(version);
    }

    /** {@inheritDoc} */
    @Override
    public String getBuildNumber() {
        return buildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BuildVersionImpl that = (BuildVersionImpl) o;

        if (buildNumber != null ? !buildNumber.equals(that.buildNumber) : that.buildNumber != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = buildNumber != null ? buildNumber.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
