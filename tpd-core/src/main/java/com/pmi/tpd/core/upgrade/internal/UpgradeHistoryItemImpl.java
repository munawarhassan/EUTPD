package com.pmi.tpd.core.upgrade.internal;

import java.util.Date;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.upgrade.IUpgradeHistoryItem;

/**
 * <p>
 * UpgradeHistoryItemImpl class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UpgradeHistoryItemImpl implements IUpgradeHistoryItem {

    /** */
    private final Date timePerformed;

    /** */
    private final String targetBuildNumber;

    /** */
    private final String targetVersion;

    /** */
    private final String originalVersion;

    /** */
    private final String originalBuildNumber;

    /** */
    private final boolean inferred;

    /**
     * <p>
     * Constructor for UpgradeHistoryItemImpl.
     * </p>
     *
     * @param timePerformed
     *            a {@link java.util.Date} object.
     * @param targetBuildNumber
     *            a {@link java.lang.String} object.
     * @param targetVersion
     *            a {@link java.lang.String} object.
     * @param originalBuildNumber
     *            a {@link java.lang.String} object.
     * @param originalVersion
     *            a {@link java.lang.String} object.
     */
    public UpgradeHistoryItemImpl(final Date timePerformed, final String targetBuildNumber, final String targetVersion,
            final String originalBuildNumber, final String originalVersion) {
        this(timePerformed, targetBuildNumber, targetVersion, originalBuildNumber, originalVersion, false);
    }

    /**
     * <p>
     * Constructor for UpgradeHistoryItemImpl.
     * </p>
     *
     * @param timePerformed
     *            a {@link java.util.Date} object.
     * @param targetBuildNumber
     *            a {@link java.lang.String} object.
     * @param targetVersion
     *            a {@link java.lang.String} object.
     * @param originalBuildNumber
     *            a {@link java.lang.String} object.
     * @param originalVersion
     *            a {@link java.lang.String} object.
     * @param inferred
     *            a boolean.
     */
    public UpgradeHistoryItemImpl(final Date timePerformed, final String targetBuildNumber, final String targetVersion,
            final String originalBuildNumber, final String originalVersion, final boolean inferred) {
        this.timePerformed = timePerformed;
        this.originalVersion = originalVersion;
        this.originalBuildNumber = originalBuildNumber;
        this.targetBuildNumber = Assert.notNull(targetBuildNumber);
        this.targetVersion = Assert.notNull(targetVersion);
        this.inferred = inferred;
    }

    /** {@inheritDoc} */
    @Override
    public Date getTimePerformed() {
        return timePerformed;
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetBuildNumber() {
        return targetBuildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public String getOriginalBuildNumber() {
        return originalBuildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getOriginalVersion() {
        return originalVersion;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInferred() {
        return inferred;
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

        final UpgradeHistoryItemImpl that = (UpgradeHistoryItemImpl) o;

        if (inferred != that.inferred) {
            return false;
        }
        if (originalBuildNumber != null ? !originalBuildNumber.equals(that.originalBuildNumber)
                : that.originalBuildNumber != null) {
            return false;
        }
        if (originalVersion != null ? !originalVersion.equals(that.originalVersion) : that.originalVersion != null) {
            return false;
        }
        if (targetBuildNumber != null ? !targetBuildNumber.equals(that.targetBuildNumber)
                : that.targetBuildNumber != null) {
            return false;
        }
        if (targetVersion != null ? !targetVersion.equals(that.targetVersion) : that.targetVersion != null) {
            return false;
        }
        if (timePerformed != null ? !timePerformed.equals(that.timePerformed) : that.timePerformed != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = timePerformed != null ? timePerformed.hashCode() : 0;
        result = 31 * result + (targetBuildNumber != null ? targetBuildNumber.hashCode() : 0);
        result = 31 * result + (targetVersion != null ? targetVersion.hashCode() : 0);
        result = 31 * result + (originalVersion != null ? originalVersion.hashCode() : 0);
        result = 31 * result + (originalBuildNumber != null ? originalBuildNumber.hashCode() : 0);
        result = 31 * result + (inferred ? 1 : 0);
        return result;
    }
}
