package com.pmi.tpd.core.model.upgrade;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IIdentityEntity;

/**
 * <p>
 * UpgradeHistoryVersion class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "UpgradeHistoryVersion")
@Table(name = UpgradeHistoryVersion.TABLE_NAME)
public class UpgradeHistoryVersion implements IIdentityEntity<String> {

    /** table name. */
    public static final String TABLE_NAME = "t_upgrade_history_version";

    /** */
    @Id()
    @Column(name = "target_build_number", length = 50)
    private String targetBuildNumber;

    /** */
    @Column(name = "target_version", length = 50)
    private String targetVersion;

    /** */
    @Column(name = "time_performed")
    private Date timePerformed;

    /**
     * <p>
     * Constructor for UpgradeHistoryVersion.
     * </p>
     */
    public UpgradeHistoryVersion() {
    }

    /**
     * <p>
     * Constructor for UpgradeHistoryVersion.
     * </p>
     *
     * @param builder
     *            a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion.Builder} object.
     */
    public UpgradeHistoryVersion(final Builder builder) {
        this.targetBuildNumber = builder.id();
        this.targetVersion = builder.targetVersion;
        this.timePerformed = builder.timePerformed;
    }

    /**
     * <p>
     * Getter for the field <code>targetBuildNumber</code>.
     * </p>
     *
     * @return the targetBuildNumber
     */
    public String getTargetBuildNumber() {
        return targetBuildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return getTargetBuildNumber();
    }

    /**
     * <p>
     * Getter for the field <code>targetVersion</code>.
     * </p>
     *
     * @return the targetVersion
     */
    public String getTargetVersion() {
        return targetVersion;
    }

    /**
     * <p>
     * Getter for the field <code>timePerformed</code>.
     * </p>
     *
     * @return the timePerformed
     */
    public Date getTimePerformed() {
        return timePerformed;
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractEntityBuilder<String, UpgradeHistoryVersion, Builder> {

        private String targetVersion;

        private Date timePerformed;

        /**
         *
         */
        public Builder() {
        }

        /**
         *
         */
        public Builder(@Nonnull final UpgradeHistoryVersion item) {
            super(item);
            this.targetVersion = item.targetVersion;
            this.timePerformed = item.timePerformed;
        }

        public Builder targetBuildNumber(final String targetBuildNumber) {
            this.id(targetBuildNumber);
            return self();
        }

        public Builder targetVersion(final String targetVersion) {
            this.targetVersion = targetVersion;
            return self();
        }

        public Builder timePerformed(final Date timePerformed) {
            this.timePerformed = timePerformed;
            return self();
        }

        @Override
        public UpgradeHistoryVersion build() {
            return new UpgradeHistoryVersion(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

    }
}
